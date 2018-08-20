package com.github.tddiaz.ddd.result;

import com.github.tddiaz.ddd.specification.Specification;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Represents the result of domain entities or value objects upon creation. The goal of this class is to handle consolidation of domain validation error responses.
 *
 * Inspired by <bold>Railway Oriented Programming</bold> and <bold>Specification and Notification Pattern</bold>.
 *
 *
 *
 * @author Tristan Diaz
 *
 */
public class Result<T> {

    /**
     * class type of domain object
     */
    private Class<T> _class;

    /**
     *  domain object as value
     */
    private T value;

    /**
     * consolidated error messages from failed specifications
     */
    private List<ErrorMessage> errors;

    /**
     * set to {@literal true} {@link #ensureFailed()} once ensure specification is failed.
     *
     * @see #ensure(Specification, String, Object)
     * @see #validateAll(Validation...)
     */
    private boolean ensureFailed = false;



    /**
     * private constructor; accepts domain object class
     *
     * @param _class class type of domain object.
     */
    private Result(Class<T> _class) {
        this._class = _class;
    }


    /**
     * private constructor; accepts domain object as value
     *
     * @param value domain object
     */
    private Result(T value) {
        this.value = value;
    }


    /**
     * static factory method
     *
     * returns Result instance with given class type of domain object
     *
     * @param _class class type of domain object.
     * @param <T> object type domain object
     * @return Result instance
     */
    public static <T> Result<T> resultFor(Class<T> _class) {
        return new Result<>(_class);
    }


    /**
     * static factory method
     *
     * returns Result instance with given domain object as value
     *
     * @param value domain object
     * @param <T> domain object type
     * @return
     */
    public static <T> Result<T> as(T value) {
        return new Result<>(value);
    }

    /**
     * to confirm Result with errors
     *
     * @return boolean
     */
    public boolean hasErrors() {
        return CollectionUtils.isNotEmpty(this.errors);
    }

    /**
     * returns domain object
     *
     * @return domain object value
     * @throws HasNoSuccessValueException if value is {@literal null}
     */
    public T get() {

        if (Objects.isNull(this.value)) {
            throw new HasNoSuccessValueException();
        }

        return this.value;
    }

    /**
     * returns list of {@link ErrorMessage}
     *
     * @return list of {@link ErrorMessage}
     */
    public List<ErrorMessage> getErrors() {
        return this.errors;
    }

    /**
     * @see #ensure(Specification, String, Object)
     */
    public Result<T> ensure(Specification specification, String message) {
        return ensure(specification, message, null);
    }


    /**
     * ensures specification is satisfied before processing further validations.
     *
     * @param specification domain specification that needs to be satisfy
     * @param message error message
     * @param actualValue actual value being validated
     *
     * @return Result
     */
    public Result<T> ensure(Specification specification, String message, Object actualValue) {

        if (hasErrors()) {
            return this;
        }

        if (!specification.isSatisfied()) {
            addError(new ErrorMessage(message, actualValue));
            ensureFailed();
        }

        return this;
    }


    /**
     * Processes every specifications validation
     *
     * @param validations array of validations
     *
     * @return Result
     */
    public Result<T> validateAll(Validation... validations) {

        if (ensureFailed) {
            return this;
        }

        for (Validation validation : validations) {
            if (!validation.specification.isSatisfied()) {
                addError(validation.errorMessage);
            }
        }

        return this;
    }


    /**
     * Combines Results from different entities or value objects
     *
     * @param results array of results
     *
     * @return Result
     */
    public Result<T> combine(Result... results) {
        for (Result result : results) {
            if (result.hasErrors()) {
                addErrors(result.getErrors());
            }
        }

        return this;
    }


    /**
     * Accepts domain entity or value object instance upon success
     *
     * @param t supplier
     * @return Result
     */
    public Result<T> onSuccess(Supplier<T> t) {

        if (!hasErrors()) {
            this.value = t.get();
        }

        return this;
    }


    /**
     * Helper method to add single ErrorMessage
     *
     * @param errorMessage ErrorMessage
     */
    private void addError(ErrorMessage errorMessage) {
        if (CollectionUtils.isEmpty(errors)) {
            this.errors = new ArrayList<>();
        }

        this.errors.add(errorMessage);
    }


    /**
     * Helper method to adds list of errorMessages
     *
     * @param errorMessages List of ErrorMessage
     */
    private void addErrors(List<ErrorMessage> errorMessages) {
        if (CollectionUtils.isEmpty(errors)) {
            this.errors = new ArrayList<>();
        }

        this.errors.addAll(errorMessages);
    }


    /**
     * Helper method to set #ensureFaield field to {@literal true}
     *
     * @see #ensureFailed
     */
    private void ensureFailed() {
        this.ensureFailed = true;
    }


    /**
     * Wrapper class for specification and error messages
     */
    public static class Validation {

        private Specification specification;
        private ErrorMessage errorMessage;

        public Validation(Specification specification, ErrorMessage errorMessage) {
            this.specification = specification;
            this.errorMessage = errorMessage;
        }

        public static Validation of(Specification specification, String message) {
            return of(specification, message, null);
        }

        public static Validation of(Specification specification, String message, Object actualValue) {
            return new Validation(specification, new ErrorMessage(message, actualValue));
        }
    }


    /**
     * Error message data object
     */
    public static class ErrorMessage {

        private String message;

        private Object actualValue;

        private ErrorMessage(String message, Object actualValue) {
            this.message = message;
            this.actualValue = actualValue;
        }

        public String getMessage() {
            return message;
        }

        public Object getActualValue() {
            return actualValue;
        }

        @Override
        public String toString() {
            return "{\"message\": \"" + this.getMessage() + "\", \"actualValue\": \"" + this.getActualValue() + "\"}";
        }

    }


    /**
     * Exception used to indicate no success value in the result.
     */
    public static class HasNoSuccessValueException extends RuntimeException {

        private static final long serialVersionUID = 2988214818106116301L;
        private static final String MESSAGE = "Result has no success value.";

        @Override
        public String getMessage() {
            return MESSAGE;
        }
    }


    @Override
    public String toString() {
        return "{\"_class\": \"" + this._class + "\", \"value\": \"" + this.value + "\", \"errors\": " + this.getErrors() + ", \"ensureFailed\": " + this.ensureFailed + "}";
    }
}
