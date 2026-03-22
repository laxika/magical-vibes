package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;



public sealed interface PermanentChoiceContext {

    record CloneCopy() implements PermanentChoiceContext {}

    record AuraGraft(UUID auraPermanentId) implements PermanentChoiceContext {}

    record LegendRule(String cardName) implements PermanentChoiceContext {}

    record BounceCreature(UUID bouncingPlayerId) implements PermanentChoiceContext {}

    record SpellRetarget(UUID spellCardId) implements PermanentChoiceContext {}

    record SacrificeCreature(UUID sacrificingPlayerId) implements PermanentChoiceContext {}

    record SacrificeCreatureThenSearchLibrary(UUID sacrificingPlayerId) implements PermanentChoiceContext {}

    record SacrificeCreatureOpponentsLoseLife(UUID sacrificingPlayerId, String sourceCardName) implements PermanentChoiceContext {}

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

    record PreventDamageSourceChoice(UUID controllerId) implements PermanentChoiceContext {}

    record RedirectDamageSourceChoice(UUID controllerId, int amount, UUID redirectTargetId) implements PermanentChoiceContext {}

    record PreventDamageToTargetFromSourceChoice(UUID controllerId, int amount, UUID targetId) implements PermanentChoiceContext {}

    record AttackTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, boolean playerTargetOnly, TargetFilter targetFilter) implements PermanentChoiceContext {

        /** Convenience constructor for any-target (permanents + players). */
        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects) {
            this(sourceCard, controllerId, effects, false, null);
        }

        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, boolean playerTargetOnly) {
            this(sourceCard, controllerId, effects, playerTargetOnly, null);
        }
    }

    record BounceOwnPermanentOrSacrificeSelf(UUID controllerId, UUID sourceCardId) implements PermanentChoiceContext {}

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

    record LibraryCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType) implements PermanentChoiceContext {}

    record SacrificeArtifactForDividedDamage(UUID controllerId, Card sourceCard, Map<UUID, Integer> damageAssignments) implements PermanentChoiceContext {}

    record ExileCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType) implements PermanentChoiceContext {}

    record GraveyardCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType) implements PermanentChoiceContext {}

    record HandCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType) implements PermanentChoiceContext {}

    record ChooseCreatureAsEnter(UUID enteringPermanentId, UUID controllerId, Card card, UUID targetId,
                                 boolean wasCastFromHand, int etbMode, boolean kicked) implements PermanentChoiceContext {}

    record LifeGainTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    /** ETB trigger that needs to target a spell on the stack (e.g. Naru Meha's copy ability). */
    record ETBSpellTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                 StackEntryPredicate spellFilter) implements PermanentChoiceContext {}

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

    /** Spell-cast trigger that needs to target a card in a graveyard (e.g. Teshar, Ancestor's Apostle). */
    record SpellGraveyardTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects) implements PermanentChoiceContext {}

    /** "Sacrifice a [permanent]. If you do, [effect]." (e.g. The First Eruption chapter III). */
    record SacrificePermanentThen(UUID controllerId, Card sourceCard, CardEffect thenEffect) implements PermanentChoiceContext {}

}
