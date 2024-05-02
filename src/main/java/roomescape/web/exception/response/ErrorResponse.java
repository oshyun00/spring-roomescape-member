package roomescape.web.exception.response;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;

public record ErrorResponse(String message, List<ErrorDetail> details) {

    public ErrorResponse(String message) {
        this(message, Collections.emptyList());
    }

    public ErrorResponse(FieldError[] errors) {
        this("", Arrays.stream(errors).map(ErrorDetail::new).toList());
    }

    public ErrorResponse(ParameterValidationResult[] parameterValidationResults) {
        this("", Arrays.stream(parameterValidationResults).map(ErrorDetail::new).toList());
    }
}
