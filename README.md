# Result and Specification - DDD

Created this POC project for the goal of having robust approach for consolidating validation error messages of domain entities and value objects.

Inspired from
* Railway Oriented Programming 
* Specification and Notification Pattern

# Example

      Result.resultFor(DomainEntity.class)
            .combine(result1, result2)
            .ensure(() -> new SpecificationImpl().isSatisfied(), "some error message")
            .ensure(() -> new SpecificationImpl().isSatisfied(), "some error message")
            .validateAll(
                    validate(() -> new SpecificationImpl().isSatisfied(), "some error message", actualValue)),
                    validate(() -> new SpecificationImpl().isSatisfied(), "some error message", actualValue)),
                    validate(() -> new SpecificationImpl().isSatisfied(), "some error message", actualValue))
            .onSuccess(() -> new DomainEntity());
