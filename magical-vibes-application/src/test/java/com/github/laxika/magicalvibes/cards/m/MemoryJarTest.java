package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.r.RielleTheEverwise;
import com.github.laxika.magicalvibes.cards.t.TamiyoCollectorOfTales;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MemoryJar.class, GiantCockroach.class})
class MemoryJarTest extends BaseCardTest {

    @Test
    @DisplayName("Activation exiles both hands face down and gives each player seven cards")
    void activationExilesHandsAndDrawsSeven() {
        List<Card> player1Hand = List.of(new MemoryJar(), new GiantCockroach());
        List<Card> player2Hand = List.of(new GiantCockroach(), new GiantCockroach(), new GiantCockroach());
        setDeck(player1, 7);
        setDeck(player2, 7);
        harness.setHand(player1, player1Hand);
        harness.setHand(player2, player2Hand);

        addReadyJar();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.exiledCards)
                .hasSize(player1Hand.size() + player2Hand.size())
                .allMatch(ExiledCardEntry::faceDown);
        harness.assertInGraveyard(player1, "Memory Jar");
    }

    @Test
    @DisplayName("The next end step discards current hands and returns the remembered cards")
    void nextEndStepReturnsRememberedCardsAfterDiscardingHands() {
        List<Card> player1Hand = List.of(new MemoryJar(), new GiantCockroach());
        List<Card> player2Hand = List.of(new GiantCockroach(), new GiantCockroach());
        setDeck(player1, 7);
        setDeck(player2, 7);
        harness.setHand(player1, player1Hand);
        harness.setHand(player2, player2Hand);

        addReadyJar();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        List<Card> replacementHand1 = List.of(new GiantCockroach(), new GiantCockroach(), new GiantCockroach());
        List<Card> replacementHand2 = List.of(new GiantCockroach(), new GiantCockroach());
        harness.setHand(player1, replacementHand1);
        harness.setHand(player2, replacementHand2);

        advanceToEndStep(player2);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(player1Hand.stream().map(Card::getId).toList());
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(player2Hand.stream().map(Card::getId).toList());
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> replacementHand1.stream().map(Card::getId).toList().contains(card.getId()))
                .containsExactlyInAnyOrderElementsOf(replacementHand1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> replacementHand2.stream().map(Card::getId).toList().contains(card.getId()))
                .containsExactlyInAnyOrderElementsOf(replacementHand2);
    }

    @Test
    @CardUsed(RielleTheEverwise.class)
    @DisplayName("The delayed discard counts the whole hand as one discard event")
    void delayedDiscardIsOneDiscardEvent() {
        List<Card> player1Hand = List.of(new MemoryJar(), new GiantCockroach());
        setDeck(player1, 9);
        setDeck(player2, 7);
        harness.setHand(player1, player1Hand);
        harness.setHand(player2, List.of());

        addReadyJar();
        addCreatureReady(player1, new RielleTheEverwise());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GiantCockroach(), new GiantCockroach()));
        harness.setHand(player2, List.of());

        advanceToEndStep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    @CardUsed(TamiyoCollectorOfTales.class)
    @DisplayName("The delayed discard respects an opponent's discard-prevention effect")
    void delayedDiscardRespectsOpponentDiscardPrevention() {
        List<Card> player1Hand = List.of(new MemoryJar(), new GiantCockroach());
        List<Card> player2Hand = List.of(new GiantCockroach());
        setDeck(player1, 7);
        setDeck(player2, 7);
        harness.setHand(player1, player1Hand);
        harness.setHand(player2, player2Hand);

        addReadyJar();
        Permanent tamiyo = harness.addToBattlefieldAndReturn(player2, new TamiyoCollectorOfTales());
        tamiyo.setCounterCount(com.github.laxika.magicalvibes.model.CounterType.LOYALTY, 5);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        List<Card> replacementHand1 = List.of(new GiantCockroach());
        List<Card> replacementHand2 = List.of(new GiantCockroach(), new GiantCockroach());
        harness.setHand(player1, replacementHand1);
        harness.setHand(player2, replacementHand2);

        advanceToEndStep(player1);

        List<Card> expectedPlayer2Hand = new ArrayList<>(player2Hand);
        expectedPlayer2Hand.addAll(replacementHand2);
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(expectedPlayer2Hand.stream().map(Card::getId).toList());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> replacementHand2.stream().map(Card::getId).toList().contains(card.getId()));
    }

    private void addReadyJar() {
        Permanent jar = harness.addToBattlefieldAndReturn(player1, new MemoryJar());
        jar.setSummoningSick(false);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    private void setDeck(Player player, int count) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            deck.add(new GiantCockroach());
        }
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(deck);
    }
}
