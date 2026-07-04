package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a spell (or ability) on the stack that targets at least one permanent satisfying
 * {@code filter}, regardless of who controls that permanent. The filter is evaluated with the
 * evaluating source's controller as the {@code sourceControllerId}, so controller-relative
 * permanent predicates (e.g. {@link PermanentControlledBySourceControllerPredicate}) work.
 *
 * <p>Used as {@code SpellCastTriggerEffect.castSpellTargetCondition} for triggers gated on the
 * cast spell's chosen targets — e.g. the "Repartee" mechanic ("cast an instant or sorcery spell
 * that targets a creature") with a {@link PermanentIsCreaturePredicate} filter.
 *
 * @param filter predicate each candidate target permanent is tested against (never {@code null})
 */
public record StackEntryTargetsPermanentPredicate(PermanentPredicate filter) implements StackEntryPredicate {
}
