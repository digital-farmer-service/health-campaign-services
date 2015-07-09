/*******************************************************************************
 * eGov suite of products aim to improve the internal efficiency,transparency, 
 *    accountability and the service delivery of the government  organizations.
 * 
 *     Copyright (C) <2015>  eGovernments Foundation
 * 
 *     The updated version of eGov suite of products as by eGovernments Foundation 
 *     is available at http://www.egovernments.org
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or 
 *     http://www.gnu.org/licenses/gpl.html .
 * 
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 * 
 * 	1) All versions of this program, verbatim or modified must carry this 
 * 	   Legal Notice.
 * 
 * 	2) Any misrepresentation of the origin of the material is prohibited. It 
 * 	   is required that all modified versions of this material be marked in 
 * 	   reasonable ways as different from the original version.
 * 
 * 	3) This license does not grant any rights to any user of the program 
 * 	   with regards to rights under trademark law for use of the trade names 
 * 	   or trademarks of eGovernments Foundation.
 * 
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 ******************************************************************************/

package org.egov.ptis.domain.entity.property;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.egov.infra.persistence.entity.AbstractAuditable;
import org.hibernate.validator.constraints.SafeHtml;

/**
 * The Class PtApplicationType.
 *
 * @author subhash
 */
@Entity
@Table(name = "EGPT_APPLICATION_TYPE")
@SequenceGenerator(name = PtApplicationType.SEQ_APPLICATION_TYPE, sequenceName = PtApplicationType.SEQ_APPLICATION_TYPE, allocationSize = 1)
@NamedQuery(name=PtApplicationType.BY_CODE, query="Select apt from PtApplicationType apt where apt.code=?")
public class PtApplicationType extends AbstractAuditable {

	protected static final String SEQ_APPLICATION_TYPE = "SEQ_EGPT_APPLICATION_TYPE";
	private static final long serialVersionUID = 1L;
	public static final String BY_CODE = "BY_CODE";

	@Id
	@GeneratedValue(generator = SEQ_APPLICATION_TYPE, strategy = GenerationType.SEQUENCE)
	private Long id;

	@NotNull
	@SafeHtml
	@Column(name = "code", unique = true)
	private String code;

	@NotNull
	@SafeHtml
	@Column(name = "name", unique = true)
	private String name;

	@NotNull
	@Column(name = "resolutiontime")
	private Long resolutionTime;

	@SafeHtml
	private String description;

	@OneToMany(mappedBy = "applicationType", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<DocumentType> documentTypes;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<DocumentType> getDocumentTypes() {
		return documentTypes;
	}

	public void setDocumentTypes(Set<DocumentType> documentTypes) {
		this.documentTypes = documentTypes;
	}

	public Long getResolutionTime() {
		return resolutionTime;
	}

	public void setResolutionTime(Long resolutionTime) {
		this.resolutionTime = resolutionTime;
	}

}
