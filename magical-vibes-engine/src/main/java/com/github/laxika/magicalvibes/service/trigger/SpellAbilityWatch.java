package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/**
 * Which of the old spell, land-play and activation slot collectors reads a triggered ability, now that the ability has
 * a real trigger (effect-slot-refactor.md step 13, family 2). Each constant is named after the slot whose collector it
 * stands for and names the trigger its cards were converted to and the zone the collector reads them in. Its
 * {@link CollectsSpellAbilityTrigger} handlers bind the event data the effects need and choose how the ability reaches
 * the stack, as the slot's handlers did, until steps 14 and 15 of the refactor replace them with trigger options and
 * effects that read the event.
 * <p>
 * Every collector of these watches reads the abilities themselves, so the legacy
 * view gives none of them and {@link #effectsOf} and {@link #registrationsOf} throw. A cast ability (a battlefield or
 * exile one without a spell filter as "any spell"), a copy ability with a spell filter and a land-play ability with a land filter or zone go to the spell-cast, spell-copy and land-play collectors,
 * which judge the filters, the options and the effects as one stack object; any other ability's options are judged by
 * its reader (its limit, event condition and intervening "if"), and its effects are handed to the watch's
 * {@link CollectsSpellAbilityTrigger} handlers, the intervening "if" wrapped around each for the check as it resolves
 * (CR 603.4). The one activation collector ({@code TriggerCollectionService.collectActivationTriggers}) reads every
 * activation ability by {@link com.github.laxika.magicalvibes.model.trigger.AbilityTrigger#unfiltered()}, once the
 * activated ability is on the stack.
 */
public enum SpellAbilityWatch implements TriggerWatch {
    /** "Whenever a mana ability of this creature resolves" (granted only: Tyvar the Bellicose). */
    ON_SELF_MANA_ABILITY_RESOLVES(Triggers.thisManaAbilityResolves(), Zone.BATTLEFIELD),
    /** "Whenever a player casts a spell". */
    ON_ANY_PLAYER_CASTS_SPELL(Triggers.playerCastsSpell(), Zone.BATTLEFIELD),
    /** "Whenever you cast a spell". */
    ON_CONTROLLER_CASTS_SPELL(Triggers.youCastSpell(), Zone.BATTLEFIELD),
    /** "Whenever you copy a spell". */
    ON_CONTROLLER_COPIES_SPELL(Triggers.youCopySpell(), Zone.BATTLEFIELD),
    /** "Whenever an opponent copies a spell". */
    ON_OPPONENT_COPIES_SPELL(Triggers.opponentCopiesSpell(), Zone.BATTLEFIELD),
    /** "Whenever you counter a spell". */
    ON_CONTROLLER_COUNTERS_SPELL(Triggers.youCounterSpell(), Zone.BATTLEFIELD),
    /** "Whenever a spell you control is countered". */
    ON_CONTROLLER_SPELL_COUNTERED(Triggers.yourSpellCountered(), Zone.BATTLEFIELD),
    /** "Whenever a spell you control causes you to gain card advantage", read by its own reader. */
    ON_CONTROLLER_SPELL_CAUSES_CARD_ADVANTAGE(Triggers.yourSpellCausesYouToGainCardAdvantage(), Zone.BATTLEFIELD),
    /** "When this spell is countered or doesn't resolve", read from the spell. */
    ON_SELF_SPELL_COUNTERED_OR_FIZZLED(Triggers.thisSpellCounteredOrFizzles(), Zone.STACK),
    /** "Whenever you play a land". */
    ON_CONTROLLER_PLAYS_LAND(Triggers.youPlayLand(), Zone.BATTLEFIELD),
    /** "Whenever you play another land". */
    ON_CONTROLLER_PLAYS_ANOTHER_LAND(Triggers.youPlayAnotherLand(), Zone.BATTLEFIELD),
    /** "Whenever an opponent plays a land". */
    ON_OPPONENT_PLAYS_LAND(Triggers.opponentPlaysLand(), Zone.BATTLEFIELD),
    /** "Whenever an opponent casts a spell". */
    ON_OPPONENT_CASTS_SPELL(Triggers.opponentCastsSpell(), Zone.BATTLEFIELD),
    /** "Whenever you cast a spell", from the graveyard. */
    GRAVEYARD_ON_CONTROLLER_CASTS_SPELL(Triggers.youCastSpell(), Zone.GRAVEYARD),
    /** "Whenever you cast a spell", from the hand. */
    HAND_ON_CONTROLLER_CASTS_SPELL(Triggers.youCastSpell(), Zone.HAND),
    /** "Whenever you cast a spell", from exile. */
    EXILE_ON_CONTROLLER_CASTS_SPELL(Triggers.youCastSpell(), Zone.EXILE),
    /** "Whenever you activate an exhaust ability", from the graveyard. */
    GRAVEYARD_ON_CONTROLLER_ACTIVATES_EXHAUST_ABILITY(Triggers.youActivateExhaustAbility(), Zone.GRAVEYARD),
    /** "Whenever you cast a spell", from the command zone. */
    COMMAND_ZONE_ON_CONTROLLER_CASTS_SPELL(Triggers.youCastSpell(), Zone.COMMAND),
    /** "When you cast this spell", read from the spell. */
    ON_SELF_CAST(Triggers.youCastThisSpell(), Zone.STACK),
    /** "Whenever you activate an ability", mana abilities included. */
    ON_CONTROLLER_ACTIVATES_ABILITY(Triggers.youActivateAbility(), Zone.BATTLEFIELD),
    /** "Whenever an opponent activates an ability", mana abilities included. */
    ON_OPPONENT_ACTIVATES_ABILITY(Triggers.opponentActivatesAbility(), Zone.BATTLEFIELD),
    /** "Whenever you activate an exhaust ability". */
    ON_CONTROLLER_ACTIVATES_EXHAUST_ABILITY(Triggers.youActivateExhaustAbility(), Zone.BATTLEFIELD),
    /** "Whenever you activate an ability that isn't a mana ability". */
    ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY(Triggers.youActivateNonManaAbility(), Zone.BATTLEFIELD),
    /** "Whenever you activate an embalm or eternalize ability". */
    ON_CONTROLLER_ACTIVATES_ETERNALIZE_OR_EMBALM(Triggers.youActivateEternalizeOrEmbalm(), Zone.BATTLEFIELD),
    /** "Whenever an opponent activates an ability that isn't a mana ability". */
    ON_OPPONENT_ACTIVATES_NONMANA_ABILITY(Triggers.opponentActivatesNonManaAbility(), Zone.BATTLEFIELD),
    /** "Whenever an ability of [a permanent] is activated, if it isn't a mana ability", whoever activates it. */
    ON_ANY_PLAYER_ACTIVATES_NONMANA_ABILITY(Triggers.playerActivatesNonManaAbility(), Zone.BATTLEFIELD);

    private final Trigger trigger;
    private final Zone zone;

    SpellAbilityWatch(Trigger trigger, Zone zone) {
        this.trigger = trigger;
        this.zone = zone;
    }

    @Override
    public Trigger trigger() {
        return trigger;
    }

    @Override
    public Zone zone() {
        return zone;
    }

    /** Throws: the collectors of a spell, land-play or activation watch read its abilities themselves. */
    @Override
    public List<CardEffect> effectsOf(Card card) {
        throw new UnsupportedOperationException(name() + " is read by abilities, not by effects");
    }

    /** Throws: the collectors of a spell, land-play or activation watch read its abilities themselves. */
    @Override
    public List<EffectRegistration> registrationsOf(Card card) {
        throw new UnsupportedOperationException(name() + " is read by abilities, not by effects");
    }
}
