package com.github.tddiaz.ddd.result;

import com.github.tddiaz.ddd.specification.Specification;
import org.junit.Test;

import static com.github.tddiaz.ddd.result.Result.HasNoSuccessValueException;
import static com.github.tddiaz.ddd.result.Result.Validation.validate;
import static com.github.tddiaz.ddd.result.Result.as;
import static com.github.tddiaz.ddd.result.Result.resultFor;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ResultTest {

    @Test
    public void givenClass_whenResultFor_shouldReturnResultInstance() {
        assertNotNull(resultFor(Object.class));
    }

    @Test
    public void givenValue_whenAs_shouldReturnResultWithValue() {
        Result<DomainEntity> result = as(new DomainEntity());
        assertNotNull(result.get());
    }

    @Test(expected = HasNoSuccessValueException.class)
    public void givenClass_whenResultForAndGet_shouldThrowError() {
        resultFor(DomainEntity.class).get();
    }

    @Test
    public void givenEnsureSpecification_whenEnsureFailed_shouldReturnErrors() {
        Result<DomainEntity> result = resultFor(DomainEntity.class)
                .ensure(() -> new FailedSpecification().isSatisfied(), "error");

        assertTrue(result.hasErrors());
        assertThat(result.getErrors(), hasSize(1));
        assertThat(result.getErrors().get(0).getMessage(), is("error"));
    }

    @Test
    public void givenMultipleEnsureSpecification_whenEnsureFails_shouldNotProceedToEnsureNextSpecification() {
        Result<DomainEntity> result = resultFor(DomainEntity.class)
                .ensure(() -> new FailedSpecification().isSatisfied(), "error")
                .ensure(() -> new FailedSpecification().isSatisfied(), "error");

        assertTrue(result.hasErrors());
        assertThat(result.getErrors(), hasSize(1));
    }

    @Test
    public void givenEnsureSpecification_whenEnsureSucceeded_shouldReturnResult() {
        Result<DomainEntity> result = resultFor(DomainEntity.class)
                .ensure(() -> new SuccessSpecification().isSatisfied(), "error");

        assertFalse(result.hasErrors());
    }

    @Test
    public void givenValidations_whenValidateAllAndHasFailures_shouldReturnErrors() {
        Result<DomainEntity> result = resultFor(DomainEntity.class)
                .validateAll(
                        validate(() -> new FailedSpecification().isSatisfied(), "error1", "actualValue1"),
                        validate(() -> new FailedSpecification().isSatisfied(), "error2", "actualValue2")
                );

        assertTrue(result.hasErrors());
        assertThat(result.getErrors(), hasSize(2));
        assertThat(result.getErrors(), hasItem(allOf(
                        hasProperty("message", is("error1")),
                        hasProperty("actualValue", is("actualValue1")))));
        assertThat(result.getErrors(), hasItem(allOf(
                hasProperty("message", is("error2")),
                hasProperty("actualValue", is("actualValue2")))));
    }

    @Test
    public void givenEnsureSpecificationAndValidations_whenEnsureFailed_shouldNotProceedProcessingFurtherValidations() {
        Result<DomainEntity> result = resultFor(DomainEntity.class)
                .ensure(() -> new FailedSpecification().isSatisfied(), "ensure failed")
                .validateAll(
                        validate(() -> new FailedSpecification().isSatisfied(), "validation failed")
                );

        assertTrue(result.hasErrors());
        assertThat(result.getErrors(), hasSize(1));
        assertThat(result.getErrors(), hasItem(allOf(
                hasProperty("message", is("ensure failed")))));
    }

    @Test
    public void givenEnsureSpecificationAndValidations_whenEnsureSucceed_shouldProceedProcessingFurtherValidations() {
        Result<DomainEntity> result = resultFor(DomainEntity.class)
                .ensure(() -> new SuccessSpecification().isSatisfied(), "ensure failed")
                .ensure(() -> new SuccessSpecification().isSatisfied(), "ensure failed")
                .validateAll(
                        validate(() -> new FailedSpecification().isSatisfied(), "validation failed"),
                        validate(() -> new FailedSpecification().isSatisfied(), "validation failed")
                );

        assertTrue(result.hasErrors());
        assertThat(result.getErrors(), hasSize(2));
    }

    @Test
    public void givenMultipleResults_whenCombine_shouldReturnConsolidatedResults() {

        Result<DomainEntity> result1 = Result.resultFor(DomainEntity.class)
                .validateAll(
                        validate(() -> new FailedSpecification().isSatisfied(), "validation failed"),
                        validate(() -> new FailedSpecification().isSatisfied(), "validation failed")
                );

        Result<DomainEntity> result2 = Result.resultFor(DomainEntity.class)
                .validateAll(
                        validate(() -> new FailedSpecification().isSatisfied(), "validation failed"),
                        validate(() -> new FailedSpecification().isSatisfied(), "validation failed")
                );

        Result<DomainEntity> domainEntityResult = Result.resultFor(DomainEntity.class)
                .combine(result1, result2);

        assertTrue(domainEntityResult.hasErrors());
        assertThat(domainEntityResult.getErrors(), hasSize(4));

    }

    @Test
    public void givenSuccessSpecification_whenOnSuccess_shouldReturnResultValue() {

        Result<DomainEntity> domainEntityResult = Result.resultFor(DomainEntity.class)
                .ensure(() -> new SuccessSpecification().isSatisfied(), "")
                .validateAll(
                        validate(() -> new SuccessSpecification().isSatisfied(), ""))
                .onSuccess(() -> new DomainEntity());

        assertFalse(domainEntityResult.hasErrors());
        assertThat(domainEntityResult.get(), notNullValue());
    }

    private static class DomainEntity {
    }

    private static class FailedSpecification implements Specification {
        @Override
        public boolean isSatisfied() {
            return false;
        }
    }

    private static class SuccessSpecification implements Specification {
        @Override
        public boolean isSatisfied() {
            return true;
        }
    }
}