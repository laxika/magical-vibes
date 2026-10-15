package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.t.TellingTime;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({ShrineOfPiercingVision.class, TellingTime.class, Shock.class})
class ShrineOfPiercingVisionTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger adds a charge counter (mandatory)")
    void upkeepTriggerAddsChargeCounter() {
        Permanent shrine = addReadyShrine(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // move to upkeep, trigger fires
        harness.passBothPriorities(); // resolve PutCountersOnSelfEffect

        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's upkeep does not add a charge counter")
    void opponentUpkeepDoesNotAddCounter() {
        Permanent shrine = addReadyShrine(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Casting a blue spell adds a charge counter")
    void castingBlueSpellAddsChargeCounter() {
        Permanent shrine = addReadyShrine(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        // Telling Time is a blue instant
        com.github.laxika.magicalvibes.cards.t.TellingTime tellingTime =
                new com.github.laxika.magicalvibes.cards.t.TellingTime();
        harness.setHand(player1, List.of(tellingTime));
        harness.castInstant(player1, 0);

        // Spell cast trigger should put charge counter on shrine
        harness.passBothPriorities(); // resolve charge counter trigger
        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a non-blue spell does not add a charge counter")
    void castingNonBlueSpellDoesNotAddCounter() {
        Permanent shrine = addReadyShrine(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.castInstant(player1, 0, player2.getId());

        // No charge counter trigger should fire — resolve Shock
        harness.passBothPriorities();

        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Sacrificing with charge counters enters library reveal choice state")
    void sacrificeEntersLibraryRevealChoiceState() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve ability from stack

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).allCards()).hasSize(3);
    }

    @Test
    @DisplayName("Choosing a card puts it into hand and the rest on the bottom")
    void choosingCardPutsInHandRestOnBottom() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 2);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);
        int originalDeckSize = deck.size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose top0 to hand; the other card goes on the bottom of the library
        harness.handleMultipleCardsChosen(player1, List.of(top0.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top0);
        assertThat(deck.get(deck.size() - 1)).isSameAs(top1);
        assertThat(deck).hasSize(originalDeckSize - 1);
    }

    @Test
    @DisplayName("Shrine is sacrificed as a cost (goes to graveyard immediately)")
    void shrineIsSacrificedAsCost() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, null, null);

        // Shrine should be in graveyard immediately (sacrifice is a cost)
        harness.assertNotOnBattlefield(player1, "Shrine of Piercing Vision");
        harness.assertInGraveyard(player1, "Shrine of Piercing Vision");
    }

    @Test
    @DisplayName("Sacrificing with zero counters does nothing")
    void sacrificeWithZeroCountersDoesNothing() {
        addReadyShrine(player1);
        // No charge counters

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve ability

        // No cards should be added to hand
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificing with 1 counter auto-puts that card into hand")
    void sacrificeWithOneCounterAutoChooses() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card topCard = deck.get(0);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Single card goes directly to hand — no choice needed
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("into their hand"));
    }

    @Test
    @DisplayName("Activated ability requires tap — tapped shrine cannot activate")
    void activatedAbilityRequiresTap() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 2);
        shrine.tap();

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability goes on the stack (not a mana ability)")
    void abilityGoesOnStack() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 2);

        int stackSizeBefore = gd.stack.size();

        harness.activateAbility(player1, 0, null, null);

        // Should add an entry to the stack (not a mana ability)
        assertThat(gd.stack.size()).isEqualTo(stackSizeBefore + 1);
    }

    @Test
    @DisplayName("More charge counters than cards in library uses available cards")
    void moreCountersThanCardsInLibrary() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 100);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        int deckSize = deck.size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Should use all available cards (capped at deck size)
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).allCards()).hasSize(deckSize);
    }

    @Test
    void remainingCardsGoToBottomInChosenOrder() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 3);
        Card first = new ShrineOfPiercingVision();
        Card chosen = new ShrineOfPiercingVision();
        Card third = new ShrineOfPiercingVision();
        Card untouched = new ShrineOfPiercingVision();
        harness.setLibrary(player1, List.of(first, chosen, third, untouched));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, third, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryDoesNotDrawOrRequestChoice() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 3);
        harness.setLibrary(player1, List.of());
        List<Card> handBefore = List.copyOf(gd.playerHands.get(player1.getId()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void countersOtherThanChargeDoNotIncreaseCardsLookedAt() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 1);
        shrine.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        Card first = new ShrineOfPiercingVision();
        Card second = new ShrineOfPiercingVision();
        harness.setLibrary(player1, List.of(first, second));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentCastingBlueSpellDoesNotAddCounter() {
        Permanent shrine = addReadyShrine(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new TellingTime()));

        harness.castInstant(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isZero();
    }

    private Permanent addReadyShrine(Player player) {
        return addCreatureReady(player, new ShrineOfPiercingVision());
    }
}
