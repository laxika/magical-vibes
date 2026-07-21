package com.github.laxika.magicalvibes.model.effect;

public interface CardEffect {

    /**
     * The declarative targeting descriptor for this effect — the single source of truth for what an
     * effect can target. Every reader consumes targeting through this ({@code targetSpec().category()},
     * {@code .predicate()}, {@code .harmful()}, {@code .selfTargeting()}, {@code .playerTargetCount()});
     * the eleven legacy per-effect {@code canTarget*} / targeting booleans that used to derive from it
     * were deleted once every reader was repointed here (TargetSpec migration step 10). Effects that
     * target nothing leave it at {@link TargetSpec#NONE}.
     *
     * <p>Two effects expose a targeting capability that a single {@link TargetSpec} cannot encode and
     * keep it on a dedicated record component instead: {@code ChangeColorTextEffect.canTargetSpell}
     * (spell OR permanent) and {@code PutCounterOnTargetPermanentEffect.targetPredicate} (a targeting
     * restriction with no cast-time gate). Read those through
     * {@code EffectResolution.targetsSpellOnStack(effect)} / {@code EffectResolution.targetPredicateOf(effect)}
     * so the component is honoured.</p>
     */
    default TargetSpec targetSpec() { return TargetSpec.NONE; }

    /**
     * Returns {@code true} if this effect is a characteristic-defining ability
     * that sets power and/or toughness (e.g. "* / * where * is ...").
     * Used by copy effects with P/T overrides (CR 707.9d): when a copy effect
     * provides specific P/T values, CDAs that define P/T are not copied.
     */
    default boolean isPowerToughnessDefining() { return false; }

    /**
     * Returns {@code true} if this ON_DEATH effect only triggers when the permanent was
     * sacrificed ("When you sacrifice this…"), not when it dies by other means. Filtered out
     * of the normal death-trigger path and collected from the sacrifice path instead.
     */
    default boolean onlyTriggersOnSacrifice() { return false; }
}
