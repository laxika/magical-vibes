package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

/**
 * Marker interface of every effect record. What the engine knows about an effect is registered once, keyed by the
 * record's class, in the place that owns that knowledge: a {@code NormalEffectHandlerBean} resolves it from the
 * stack, a {@code StaticEffectHandlerBean} applies it as a static ability, a {@code MayEffectHandlerBean} carries
 * out the answer to its "you may", {@link #targetSpec()} declares what it targets (a {@code @ValidatesTarget}
 * method adds only what a spec cannot express), and the capability interfaces of this package
 * ({@link DamageDealingEffect}, {@link RemovalEffect}, {@link ManaProducingEffect}, {@link CardDrawingEffect},
 * {@link LifeGainEffect}, {@link TokenCreatingEffect}, {@link CreatureBoostEffect}, {@link StaticCreatureBoostEffect},
 * {@link KeywordGrantingEffect}, {@link ControlStealingEffect}, {@link CounterSpellingEffect},
 * {@link RegenerationEffect}) tell the AI what it does. Engine and AI code outside the handler and validator
 * packages reads those, not {@code instanceof} of a concrete effect; no build check enforces this.
 */
public interface CardEffect {
    /**
     * The zones a static ability made of this effect functions from: the battlefield (CR 113.6),
     * unless the rule for this kind of ability names other zones. {@code Card.addStaticEffect(effect)}
     * and {@code StaticAbility.of} use it.
     */
    default AbilityZones staticAbilityZones() { return AbilityZones.BATTLEFIELD; }

    /** Whether paying for this effect is a special action that does not use the stack. */
    default boolean isSpecialAction() { return false; }

    /** Whether this pending choice must finish before the next card of a draw instruction. */
    default boolean pausesDrawInstruction() { return false; }

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
     * Returns the positional target-group index used by effects that resolve against a declared
     * group rather than the entry's ordinary target. A negative value means no explicit group.
     */
    default int targetGroup() { return -1; }

    /**
     * Returns whether an upkeep trigger using this effect has its permanent target chosen by the
     * active player rather than by the ability's controller.
     */
    default boolean targetChosenByActivePlayer() { return false; }

    /**
     * Returns whether this effect's target is "of an opponent's choice" (Karplusan Minotaur): as a triggered ability
     * using it goes on the stack, its controller chooses one of their opponents, who chooses the target.
     */
    default boolean targetChosenByAnOpponent() { return false; }

    /**
     * Returns whether a death trigger using this effect has its target chosen, as it is put on the stack, by the
     * controller of its triggering permanent as that permanent last existed on the battlefield (Necrotic Plague's
     * "its controller chooses target creature"), rather than by the ability's controller.
     */
    default boolean targetChosenByTriggeringPermanentController() { return false; }

    /**
     * Returns whether a target is selected randomly as this triggered ability is put onto the
     * stack, rather than chosen by a player.
     */
    default boolean targetChosenAtRandom() { return false; }

    /**
     * Returns whether this effect reads the numeric value captured from the event that triggered
     * its stack entry. Death-trigger collectors use this to snapshot last-known information before
     * the triggering permanent leaves the battlefield.
     */
    default boolean referencesEventValue() { return false; }

    /**
     * Returns {@code true} when an activated ability with this effect may target cards anywhere in
     * exile rather than only cards exiled with its source permanent.
     */
    default boolean targetsAllExiledCardsInAbility() { return false; }

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

    /** Whether this effect notes the mana value of cards as they enter exile. */
    default boolean notesManaValueOfExiledCards() { return false; }

    /**
     * Returns {@code true} if the card-level target group bound to this effect belongs to a
     * reflexive "when you do" ability it creates (CR 603.12), not to the ability carrying it. Those
     * targets are chosen as the reflexive ability is put on the stack, after the optional action,
     * so the carrying trigger must not choose them when it triggers.
     */
    default boolean targetsChosenByReflexiveAbility() { return false; }

    /**
     * Returns {@code true} when this effect's declared target may be omitted, such as an
     * "up to one target" choice made while a triggered ability is put on the stack.
     */
    default boolean hasOptionalTarget() { return false; }

    /**
     * Returns {@code true} when everything this effect does is done to its declared targets, so it
     * does nothing when its ability was put on the stack with none chosen ("any number of target
     * Equipment" with zero chosen, CR 601.2c, 603.3d). A "you may" wrapping only such an effect then
     * has no choice to offer and isn't asked.
     */
    default boolean actsOnlyOnItsTargets() { return false; }

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

    /**
     * Returns whether a hand-choice follow-up reads the permanent just put onto the battlefield.
     * The hand-choice completion code binds that permanent as the follow-up's chosen reference.
     */
    default boolean usesChosenPermanentReference() { return false; }

    /**
     * Returns whether this effect or one of its nested effects branches on the source ability's
     * resolution count for the current turn, either through an explicit condition or a dynamic
     * amount that reads the count.
     */
    default boolean hasAbilityResolutionCondition() { return false; }

    /**
     * The modal whose modes the controller chooses as a triggered ability made of this effect alone is put on the
     * stack (CR 603.3c), or {@code null} for an effect whose ability chooses no mode then.
     */
    default ChooseOneEffect modesChosenAsItTriggers() { return null; }

    /**
     * The most modes that may be chosen as a triggered ability made of this effect alone is put on the stack ("choose
     * up to X", X read then), or {@code null} for the modal's own count.
     */
    default com.github.laxika.magicalvibes.model.amount.DynamicAmount maximumModesChosenAsItTriggers() { return null; }

    /**
     * The modal this effect is ("choose one —"), whatever its modes depend on, or {@code null} for an effect that isn't
     * one: the stack door chooses the mode of a triggered ability made of it alone as it goes on the stack when the
     * mode can't be announced once the ability is there (CR 603.3c).
     */
    default ChooseOneEffect modal() { return null; }

    /**
     * The modes one of which is chosen at random as a triggered ability with this effect is put on the stack ("choose
     * one at random", CR 603.3c: a mode that would be illegal can't be chosen), the effect then being the chosen mode's;
     * {@code null} for an effect whose ability chooses no mode at random.
     */
    default java.util.List<ChooseOneEffect.ChooseOneOption> modesChosenAtRandomAsItTriggers() { return null; }

    /**
     * Whether this effect divides an amount among its targets as its ability is put on the stack ("distribute three +1/+1
     * counters among one, two, or three target creatures", CR 601.2d): its targets are chosen one at a time, the
     * division after them.
     */
    default boolean dividesAmongTargetsAsItGoesOnTheStack() { return false; }

    /**
     * The graveyard cards this effect chooses as its ability is put on the stack when they aren't one declared target
     * ("exile up to two target creature cards from defending player's graveyard"), looking through a "you may" or an
     * intervening "if" around it; {@code null} for an effect that chooses none.
     */
    default GraveyardCardChoosingEffect graveyardCardChoice() {
        return this instanceof GraveyardCardChoosingEffect choosing && choosing.choosesGraveyardCards()
                ? choosing : null;
    }
}
