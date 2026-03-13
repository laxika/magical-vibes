package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

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

    record ActivatedAbilityCostChoice(UUID activatingPlayerId,
                                      UUID sourcePermanentId,
                                      Integer abilityIndex,
                                      Integer xValue,
                                      UUID targetPermanentId,
                                      Zone targetZone,
                                      CardEffect costEffect,
                                      int remaining) implements PermanentChoiceContext {}

    record DeathTriggerTarget(Card dyingCard, UUID controllerId, List<CardEffect> effects) implements PermanentChoiceContext {}

    record DiscardTriggerAnyTarget(Card discardedCard, UUID controllerId, List<CardEffect> effects) implements PermanentChoiceContext {}

    record MayAbilityTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects) implements PermanentChoiceContext {}

    record PreventDamageSourceChoice(UUID controllerId) implements PermanentChoiceContext {}

    record RedirectDamageSourceChoice(UUID controllerId, int amount, UUID redirectTargetId) implements PermanentChoiceContext {}

    record AttackTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, boolean playerTargetOnly) implements PermanentChoiceContext {

        /** Convenience constructor for any-target (permanents + players). */
        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects) {
            this(sourceCard, controllerId, effects, false);
        }
    }

    record BounceOwnPermanentOrSacrificeSelf(UUID controllerId, UUID sourceCardId) implements PermanentChoiceContext {}

    record EmblemTriggerTarget(String emblemDescription, UUID controllerId, List<CardEffect> effects, Card sourceCard) implements PermanentChoiceContext {}

    record UpkeepCopyTriggerTarget(Card sourceCard, UUID controllerId, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record CapriciousEfreetOwnTarget(Card sourceCard, UUID controllerId, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record LibraryCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType) implements PermanentChoiceContext {}

    record SacrificeArtifactForDividedDamage(UUID controllerId, Card sourceCard, Map<UUID, Integer> damageAssignments) implements PermanentChoiceContext {}

    record ExileCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType) implements PermanentChoiceContext {}

    record GraveyardCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType) implements PermanentChoiceContext {}

    record HandCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType) implements PermanentChoiceContext {}

}
