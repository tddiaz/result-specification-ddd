package com.github.tddiaz.ddd.specification;

/**
 * Interface class to be implemented for creating domain specification
 *
 * @author Tristan Diaz
 */
@FunctionalInterface
public interface Specification {
    boolean isSatisfied();
}
