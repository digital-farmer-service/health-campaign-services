package org.egov.common.utils;

import org.egov.common.models.Error;

import java.util.List;
import java.util.Map;

public interface Validator<R, T> {
    Map<T, List<Error>> validate(R r);
}
