package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

public interface CardEffect {

    /**
     * The declarative targeting descriptor for this effect — the single source of truth for what an
     * effect can target. Every reader consumes targeting through this ({@code targetSpec().declaredTarget()},
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
     * Which players this effect may target, for the effects whose wording narrows the player half
     * of an otherwise shared {@link TargetSpec} — "target <em>opponent</em> or planeswalker" against
     * "target <em>player</em> or planeswalker", which declare the same
     * {@code TargetPredicates.playerOrPlaneswalker()}. The declared target cannot tell them apart,
     * so a reader that builds a player target list must consult this rather than infer the
     * restriction from the spec's shape.
     */
    default PlayerRelation targetPlayerRelation() { return PlayerRelation.ANY; }

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

    /**
     * Returns whether this effect should trigger for the controller's current draw count this turn.
     * Effects that do not restrict a draw count always return {@code true}.
     */
    default boolean triggersOnControllerDrawCount(int cardsDrawnThisTurn) { return true; }

    /**
     * Returns {@code true} if this effect resolves against the permanent its source Aura/Equipment
     * is attached to rather than against a chosen target. The activation path captures that attached
     * permanent as the ability's target before any sacrifice cost severs the attachment, so the
     * effect still finds it at resolution ("Sacrifice this Aura: Return enchanted creature to its
     * owner's hand." — Phantom Wings).
     */
    default boolean resolvesAgainstAttachedPermanent() { return false; }

    /**
     * Returns {@code true} when an effect must receive resolution even if all of its declared
     * targets have become illegal, so its handler can apply a separate resolution clause.
     */
    default boolean resolvesWhenTargetIllegal() { return false; }
}
