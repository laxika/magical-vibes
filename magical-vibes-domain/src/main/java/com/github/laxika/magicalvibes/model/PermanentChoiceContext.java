package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;



public sealed interface PermanentChoiceContext extends PendingInteraction {

    record CloneCopy() implements PermanentChoiceContext {}

    record AuraGraft(UUID auraPermanentId) implements PermanentChoiceContext {}

    record LegendRule(String cardName) implements PermanentChoiceContext {}

    record BounceCreature(UUID bouncingPlayerId) implements PermanentChoiceContext {}

    record SpellRetarget(UUID spellCardId) implements PermanentChoiceContext {}

    record SacrificeCreature(UUID sacrificingPlayerId) implements PermanentChoiceContext {}

    record SacrificeCreatureThenSearchLibrary(UUID sacrificingPlayerId) implements PermanentChoiceContext {}

    record SacrificeCreatureOpponentsLoseLife(UUID sacrificingPlayerId, String sourceCardName) implements PermanentChoiceContext {}

    record ForcedCostOrElse(UUID controllerId, UUID sourcePermanentId, Card sourceCard,
                            com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect effect) implements PermanentChoiceContext {}

    record SacrificeCreatureControllerGainsLifeEqualToToughness(UUID sacrificingPlayerId, UUID controllerId, String sourceCardName) implements PermanentChoiceContext {}

    record ActivatedAbilityCostChoice(UUID activatingPlayerId,
                                      UUID sourcePermanentId,
                                      Integer abilityIndex,
                                      Integer xValue,
                                      UUID targetId,
                                      Zone targetZone,
                                      CardEffect costEffect,
                                      int remaining) implements PermanentChoiceContext {}

    record DeathTriggerTarget(Card dyingCard, UUID controllerId, List<CardEffect> effects) implements PermanentChoiceContext {}

    record DiscardTriggerAnyTarget(Card discardedCard, UUID controllerId, List<CardEffect> effects) implements PermanentChoiceContext {}

    record MayAbilityTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects) implements PermanentChoiceContext {}

    record PreventDamageSourceChoice(UUID controllerId, boolean controllerOnly, Set<CardColor> colorFilter)
            implements PermanentChoiceContext {

        public PreventDamageSourceChoice(UUID controllerId) {
            this(controllerId, true, Set.of());
        }
    }

    record RedirectDamageSourceChoice(UUID controllerId, int amount, UUID redirectTargetId) implements PermanentChoiceContext {}

    record PreventDamageToTargetFromSourceChoice(UUID controllerId, int amount, UUID targetId) implements PermanentChoiceContext {}

    record AttackTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, boolean playerTargetOnly, TargetFilter targetFilter, int spellManaSpentX) implements PermanentChoiceContext {

        /** Convenience constructor for any-target (permanents + players). */
        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects) {
            this(sourceCard, controllerId, effects, false, null, 0);
        }

        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, boolean playerTargetOnly) {
            this(sourceCard, controllerId, effects, playerTargetOnly, null, 0);
        }

        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, boolean playerTargetOnly, TargetFilter targetFilter) {
            this(sourceCard, controllerId, effects, playerTargetOnly, targetFilter, 0);
        }
    }

    record BounceOwnPermanentOrSacrificeSelf(UUID controllerId, UUID sourceCardId) implements PermanentChoiceContext {}

    /** Champion a creature: exile the chosen creature until the source permanent leaves the battlefield. */
    record ChampionCreature(UUID sourcePermanentId, UUID controllerId) implements PermanentChoiceContext {}

    record EmblemTriggerTarget(String emblemDescription, UUID controllerId, List<CardEffect> effects, Card sourceCard, boolean opponentControlledOnly) implements PermanentChoiceContext {
        /** Convenience constructor for backwards compatibility (targets any permanent). */
        public EmblemTriggerTarget(String emblemDescription, UUID controllerId, List<CardEffect> effects, Card sourceCard) {
            this(emblemDescription, controllerId, effects, sourceCard, false);
        }
    }

    record UpkeepPlayerTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record UpkeepMultiPlayerTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record UpkeepSecondPlayerTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId, UUID firstTargetPlayerId) implements PermanentChoiceContext {}

    record UpkeepCopyTriggerTarget(Card sourceCard, UUID controllerId, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record CapriciousEfreetOwnTarget(Card sourceCard, UUID controllerId, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record EndStepTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record BeginningOfCombatTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record LibraryCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType) implements PermanentChoiceContext {}

    record SacrificeArtifactForDividedDamage(UUID controllerId, Card sourceCard, Map<UUID, Integer> damageAssignments) implements PermanentChoiceContext {}

    record ExileCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType,
                                boolean copy, List<UUID> chosenTargets) implements PermanentChoiceContext {
        // {@code copy=true} marks a Paradigm copy that must cease to exist rather than being placed in
        // a zone (CR 707.10a) — both on resolution and when it can't be legally cast. Defaults to false
        // for real cards cast from exile.
        // {@code chosenTargets} accumulates already-selected targets, in the card's declared target
        // order, while a multi-target spell walks its target slots one at a time. Empty for the
        // single-target path (which stores its lone target as the StackEntry's {@code targetId}).
        public ExileCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType, boolean copy) {
            this(cardToCast, controllerId, spellEffects, spellType, copy, List.of());
        }

        public ExileCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType) {
            this(cardToCast, controllerId, spellEffects, spellType, false, List.of());
        }
    }

    record GraveyardCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType) implements PermanentChoiceContext {}

    record HandCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType) implements PermanentChoiceContext {}

    record ChooseCreatureAsEnter(UUID enteringPermanentId, UUID controllerId, Card card, UUID targetId,
                                 boolean wasCastFromHand, int etbMode, boolean kicked) implements PermanentChoiceContext {}

    record LifeGainTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    /** "Whenever a creature enters from your graveyard, that creature deals damage equal to its power to
     *  any target." The {@code sourcePermanentId} points at the creature that entered (the damage source);
     *  {@code sourceCard} is the permanent whose ability triggered (e.g. Flayer of the Hatebound). */
    record EntersFromGraveyardTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    /** ETB trigger that needs to target a spell on the stack (e.g. Naru Meha's copy ability). */
    record ETBSpellTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                 StackEntryPredicate spellFilter) implements PermanentChoiceContext {}

    /**
     * ETB trigger on a token copy that needs to choose a target at trigger time (CR 603.3).
     * Used when a token copy is created of a creature with a targeted ETB ability
     * (e.g. Cackling Counterpart → Homarid Explorer). The target can't be chosen at cast
     * time because the token wasn't cast — it's created directly on the battlefield.
     */
    record ETBTokenTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                 UUID sourcePermanentId, TargetFilter targetFilter) implements PermanentChoiceContext {}

    /**
     * ETB trigger on a token copy of a creature with multiple target groups or groups with
     * {@code maxTargets > 1} (e.g. Burning Sun's Avatar: mandatory opponent/planeswalker +
     * optional creature; or "up to 3 target creatures"). Targets are chosen slot-by-slot at
     * trigger time: each group can accept up to {@code maxTargets} targets before advancing.
     * Chosen targets accumulate in {@code chosenTargetsSoFar}. A response equal to
     * {@code controllerId} signals "done with this group" — only valid once the group's
     * minimum has been met.
     */
    record ETBTokenMultiTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                      UUID sourcePermanentId, List<UUID> chosenTargetsSoFar,
                                      int currentGroupIndex, int chosenInCurrentGroup) implements PermanentChoiceContext {}

    /** Saga chapter ability that targets a permanent (e.g. Phyrexian Scriptures chapter I).
     *  {@code targetFilters} restricts valid targets (e.g. "creature an opponent controls"); null/empty = any creature. */
    record SagaChapterTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                             UUID sourcePermanentId, String chapterName,
                             Set<TargetFilter> targetFilters) implements PermanentChoiceContext {}

    /** Saga chapter ability that targets a card in a graveyard (e.g. The Mirari Conjecture chapters I/II). */
    record SagaChapterGraveyardTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                      UUID sourcePermanentId, String chapterName) implements PermanentChoiceContext {}

    record GraveyardAbilityCostChoice(UUID activatingPlayerId,
                                      Card graveyardCard,
                                      int graveyardCardIndex,
                                      Integer abilityIndex,
                                      CardEffect costEffect,
                                      int remaining) implements PermanentChoiceContext {}

    /** Tap-cost payment for a resolution-time may ability (e.g. Aziza, Mage Tower Captain). */
    record MayAbilityTapCostChoice(UUID playerId,
                                   UUID sourcePermanentId,
                                   com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost costEffect,
                                   int remaining) implements PermanentChoiceContext {}

    /** Spell-cast trigger that needs to target a card in a graveyard (e.g. Teshar, Ancestor's Apostle). */
    record SpellGraveyardTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects) implements PermanentChoiceContext {}

    /** "Sacrifice a [permanent]. If you do, [effect]." (e.g. The First Eruption chapter III). */
    record SacrificePermanentThen(UUID controllerId, Card sourceCard, CardEffect thenEffect) implements PermanentChoiceContext {}

    /** "Sacrifice a creature. If you do, create X tokens, where X is its toughness." (e.g. Feed the Pack). */
    record SacrificeCreatureCreateTokensEqualToToughness(UUID controllerId, Card sourceCard,
                                                         com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate) implements PermanentChoiceContext {}

    /** Explore trigger that needs to target a creature an opponent controls
     *  (e.g. Lurking Chupacabra: "Whenever a creature you control explores, target creature
     *  an opponent controls gets -2/-2 until end of turn."). */
    record ExploreTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    /** Clash trigger ({@code EffectSlot.ON_CONTROLLER_CLASHES}) that needs to target a creature an
     *  opponent controls (e.g. Entangling Trap: "Whenever you clash, tap target creature an opponent
     *  controls. If you won, ..."). The {@code effects} have already been resolved for the clash
     *  outcome (win-conditional effects included only on a won clash). */
    record ClashTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    /** Transform trigger that first chooses a target opponent, then up to one creature that player controls. */
    record TransformOpponentThenCreatureTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                               UUID sourcePermanentId) implements PermanentChoiceContext {}

    /** Creature choices for TransformOpponentThenCreatureTarget. Choosing controllerId means no more creature targets. */
    record TransformCreatureTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                   UUID sourcePermanentId, UUID opponentId, List<UUID> creatureIds,
                                   int maxCreatureTargets) implements PermanentChoiceContext {}

}
