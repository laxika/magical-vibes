package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/**
 * Which of the old library, draw and discard slot collectors reads a triggered ability, now that the ability has a
 * real trigger (effect-slot-refactor.md step 13, family 7). Each constant is named after the slot whose collector it
 * stands for and names the trigger its cards were converted to and the zone the collector reads them in. The
 * collectors in {@code DrawService}, {@code TriggerCollectionService}, {@code LibrarySearchTriggerHelper} and
 * {@code LibraryShuffleHelper} still decide when the ability triggers, how its effects group into stack objects and
 * which event data it is bound to, as the slot's did, until step 15 of the refactor replaces them with effects that
 * read the event. A "this card is cycled or discarded" ability
 * functions from the zone the card winds up in; its collectors read it as a graveyard ability wherever the card is.
 * <p>
 * Every collector of these watches reads the abilities themselves, through
 * {@link LibraryAbilityReader}: the discarded card's filter, a draw's count, "for the first time each turn", "you cycle
 * a card", "an opponent causes you to discard a card", the draw-step draws and the limit come from the ability, and the
 * legacy view gives none of them, so {@link #effectsOf} and {@link #registrationsOf} throw.
 */
public enum LibraryWatch implements TriggerWatch {
    /** "Whenever you draw a card", once for each card. */
    ON_CONTROLLER_DRAWS(Triggers.youDrawCard(), Zone.BATTLEFIELD),
    /** "Whenever you draw a card", from the graveyard: "your second card each turn" (Wolfbat). */
    GRAVEYARD_ON_CONTROLLER_DRAWS(Triggers.youDrawCard(), Zone.GRAVEYARD),
    /** "Whenever an opponent draws a card", from the graveyard: "their second card each turn" (Dogged Detective). */
    GRAVEYARD_ON_OPPONENT_DRAWS(Triggers.opponentDrawsCard(), Zone.GRAVEYARD),
    /** "Whenever an opponent draws a card", once for each card. */
    ON_OPPONENT_DRAWS(Triggers.opponentDrawsCard(), Zone.BATTLEFIELD),
    /** "Whenever enchanted player draws a card", once for each card. */
    ON_ENCHANTED_PLAYER_DRAWS(Triggers.enchantedPlayerDrawsCard(), Zone.BATTLEFIELD),
    /** "Whenever you reveal a [card] this way", once for each card the source revealed as it was drawn. */
    ON_CONTROLLER_REVEALS_CARD_THIS_WAY(Triggers.youRevealCardThisWay(), Zone.BATTLEFIELD),
    /** "Whenever an opponent discards a card", once for each card, cycling included. */
    ON_OPPONENT_DISCARDS(Triggers.opponentDiscardsCard(), Zone.BATTLEFIELD),
    /** "Whenever you discard a card", once for each card, cycling included. */
    ON_CONTROLLER_DISCARDS(Triggers.youDiscardCard(), Zone.BATTLEFIELD),
    /** "Whenever you discard one or more cards", once for each discard event. */
    ON_CONTROLLER_DISCARD_EVENT(Triggers.youDiscardOneOrMoreCards(), Zone.BATTLEFIELD),
    /** "Whenever a player cycles a card". */
    ON_ANY_PLAYER_CYCLES(Triggers.playerCyclesCard(), Zone.BATTLEFIELD),
    /** "When you cycle this card". */
    ON_SELF_CYCLED(Triggers.youCycleThisCard(), Zone.GRAVEYARD),
    /** "When you discard this card", whatever causes the discard. */
    ON_SELF_DISCARDED(Triggers.youDiscardThisCard(), Zone.GRAVEYARD),
    /** "When a spell or ability an opponent controls causes you to discard this card". */
    ON_SELF_DISCARDED_BY_OPPONENT(Triggers.opponentCausesYouToDiscardThisCard(), Zone.GRAVEYARD),
    /** "Whenever you scry". */
    ON_CONTROLLER_SCRIES(Triggers.youScry(), Zone.BATTLEFIELD),
    /** "Whenever an opponent scries". */
    ON_OPPONENT_SCRIES(Triggers.opponentScries(), Zone.BATTLEFIELD),
    /** "Whenever you surveil". */
    ON_CONTROLLER_SURVEILS(Triggers.youSurveil(), Zone.BATTLEFIELD),
    /** "Whenever you surveil", from the graveyard. */
    GRAVEYARD_ON_CONTROLLER_SURVEILS(Triggers.youSurveil(), Zone.GRAVEYARD),
    /** "Whenever an opponent surveils". */
    ON_OPPONENT_SURVEILS(Triggers.opponentSurveils(), Zone.BATTLEFIELD),
    /** "Whenever you search your library". */
    ON_CONTROLLER_SEARCHES_LIBRARY(Triggers.youSearchYourLibrary(), Zone.BATTLEFIELD),
    /** "Whenever an opponent searches their library". */
    ON_OPPONENT_SEARCHES_LIBRARY(Triggers.opponentSearchesTheirLibrary(), Zone.BATTLEFIELD),
    /** "Whenever an opponent shuffles their library". */
    ON_OPPONENT_SHUFFLES_LIBRARY(Triggers.opponentShufflesLibrary(), Zone.BATTLEFIELD),
    /** "Whenever a player shuffles their library". */
    ON_ANY_PLAYER_SHUFFLES_LIBRARY(Triggers.playerShufflesLibrary(), Zone.BATTLEFIELD),
    /** "Whenever a spell or ability causes its controller to shuffle their library". */
    ON_SPELL_OR_ABILITY_CAUSES_CONTROLLER_SHUFFLE(
            Triggers.spellOrAbilityCausesItsControllerToShuffle(), Zone.BATTLEFIELD);

    private final Trigger trigger;
    private final Zone zone;

    LibraryWatch(Trigger trigger, Zone zone) {
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

    /** Throws: the collectors of a library watch read its abilities themselves ({@link LibraryAbilityReader}). */
    @Override
    public List<CardEffect> effectsOf(Card card) {
        throw new UnsupportedOperationException(name() + " is read by abilities, not by effects");
    }

    /** Throws: the collectors of a library watch read its abilities themselves ({@link LibraryAbilityReader}). */
    @Override
    public List<EffectRegistration> registrationsOf(Card card) {
        throw new UnsupportedOperationException(name() + " is read by abilities, not by effects");
    }
}
