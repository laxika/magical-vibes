package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The targets of a Saga chapter ability, chosen as the ability is put on the stack.
 *
 * @param filters  the filters of the chapter's single target, in the order they were given
 * @param groups   the target groups of a chapter with more than one target
 * @param required whether the single target is mandatory (no "up to one"), so it can't be skipped
 *                 while a legal one exists
 */
public record SagaChapterTargets(Set<TargetFilter> filters, List<SagaChapterTargetGroup> groups,
                                 boolean required) {

    /** A chapter without targets. */
    public static final SagaChapterTargets NONE = new SagaChapterTargets(Set.of(), List.of(), false);

    public SagaChapterTargets {
        filters = Collections.unmodifiableSet(new LinkedHashSet<>(Objects.requireNonNull(filters, "filters")));
        groups = List.copyOf(Objects.requireNonNull(groups, "groups"));
    }

    public SagaChapterTargets withFilters(Set<TargetFilter> filters) {
        return new SagaChapterTargets(filters, groups, required);
    }

    public SagaChapterTargets withGroups(List<SagaChapterTargetGroup> groups) {
        return new SagaChapterTargets(filters, groups, required);
    }

    public SagaChapterTargets withRequired() {
        return new SagaChapterTargets(filters, groups, true);
    }
}
