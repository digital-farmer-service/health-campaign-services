package org.egov.product.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.models.coremodels.AuditDetails;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.data.query.builder.SelectQueryBuilder;
import org.egov.common.data.query.exception.QueryBuilderException;
import org.egov.common.producer.Producer;
import org.egov.product.repository.rowmapper.ProductVariantRowMapper;
import org.egov.product.web.models.AdditionalFields;
import org.egov.product.web.models.ProductVariant;
import org.egov.product.web.models.ProductVariantSearch;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class ProductVariantRepository {

    private final Producer producer;

    private final RedisTemplate<String, Object> redisTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final SelectQueryBuilder selectQueryBuilder;

    private static final String HASH_KEY = "product-variant";

    @Autowired
    public ProductVariantRepository(Producer producer, RedisTemplate<String, Object> redisTemplate,
                                    NamedParameterJdbcTemplate namedParameterJdbcTemplate, SelectQueryBuilder selectQueryBuilder) {
        this.producer = producer;
        this.redisTemplate = redisTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.selectQueryBuilder = selectQueryBuilder;
    }

    public List<ProductVariant> save(List<ProductVariant> productVariants, String topic) {
        producer.push(topic, productVariants);
        putInCache(productVariants);
        return productVariants;
    }

    public List<ProductVariant> findById(List<String> ids) {
        Collection<Object> collection = new ArrayList<>(ids);
        ArrayList<ProductVariant> variantsFound = new ArrayList<>();
        List<Object> productVariants = redisTemplate.opsForHash()
                .multiGet(HASH_KEY, collection);
        if (!productVariants.isEmpty()) {
            log.info("Cache hit");
            variantsFound = (ArrayList<ProductVariant>) productVariants.stream().map(ProductVariant.class::cast)
                    .collect(Collectors.toList());
            // return only if all the variants are found in cache
            ids.removeAll(variantsFound.stream().map(ProductVariant::getId).collect(Collectors.toList()));
            if (ids.isEmpty()) {
                return variantsFound;
            }
        }
        String query = "SELECT * FROM product_variant WHERE id IN (:ids) and isDeleted = false";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("ids", ids);
        variantsFound.addAll(namedParameterJdbcTemplate.queryForObject(query, paramMap,
                    ((resultSet, i) -> {
                        List<ProductVariant> pvList = new ArrayList<>();
                        try {
                            mapRow(resultSet, pvList);
                            while (resultSet.next()) {
                                mapRow(resultSet, pvList);
                            }
                        } catch (Exception e) {
                            throw new CustomException("ERROR_IN_SELECT", e.getMessage());
                        }
                        putInCache(pvList);
                        return pvList;
                    })));
        return variantsFound;
    }

    private void putInCache(List<ProductVariant> productVariants) {
        Map<String, ProductVariant> productVariantMap = productVariants.stream()
                .collect(Collectors
                        .toMap(ProductVariant::getId,
                                productVariant -> productVariant));
        redisTemplate.opsForHash().putAll(HASH_KEY, productVariantMap);
    }

    public List<ProductVariant> find(ProductVariantSearch productVariantSearch,
                                     Integer limit,
                                     Integer offset,
                                     String tenantId,
                                     Long lastChangedSince,
                                     Boolean includeDeleted) throws QueryBuilderException {
        String query = selectQueryBuilder.build(productVariantSearch);
        query += " AND tenantId = :tenantId ";
        if (!includeDeleted) {
            query += " AND isDeleted = :isDeleted ";
        }
        if (lastChangedSince != null) {
            query += " AND lastModifiedTime >= :lastModifiedTime ";
        }
        query += "ORDER BY id ASC LIMIT :limit OFFSET :offset";
        Map<String, Object> paramsMap = selectQueryBuilder.getParamsMap();
        paramsMap.put("tenantId", tenantId);
        paramsMap.put("isDeleted", includeDeleted);
        paramsMap.put("lastModifiedTime", lastChangedSince);
        paramsMap.put("limit", limit);
        paramsMap.put("offset", offset);
        return namedParameterJdbcTemplate.query(query, paramsMap, new ProductVariantRowMapper());
    }

    private void mapRow(ResultSet resultSet, List<ProductVariant> pvList) throws SQLException, JsonProcessingException {
        ProductVariant pv = ProductVariant.builder()
                .id(resultSet.getString("id"))
                .productId(resultSet.getString("productId"))
                .tenantId(resultSet.getString("tenantId"))
                .sku(resultSet.getString("sku"))
                .variation(resultSet.getString("variation"))
                .isDeleted(resultSet.getBoolean("isDeleted"))
                .rowVersion(resultSet.getInt("rowVersion"))
                .additionalFields(new ObjectMapper()
                        .readValue(resultSet.getString("additionalDetails"),
                                AdditionalFields.class))
                .auditDetails(AuditDetails.builder()
                        .lastModifiedTime(resultSet.getLong("lastModifiedTime"))
                        .createdTime(resultSet.getLong("createdTime"))
                        .createdBy(resultSet.getString("createdBy"))
                        .lastModifiedBy(resultSet.getString("lastModifiedBy"))
                        .build())
                .build();
        pvList.add(pv);
    }
}
