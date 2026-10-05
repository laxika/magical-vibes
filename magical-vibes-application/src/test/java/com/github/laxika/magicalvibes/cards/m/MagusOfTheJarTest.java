package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagusOfTheJar.class, Forest.class, Island.class})
class MagusOfTheJarTest extends BaseCardTest {

    @Test
    @DisplayName("Activation exiles both hands face down and gives each player seven cards")
    void activationExilesHandsAndDrawsSeven() {
        List<Card> player1Hand = List.of(new MagusOfTheJar(), new Forest());
        List<Card> player2Hand = List.of(new Forest(), new Island(), new Forest());
        setDeck(player1, 7);
        setDeck(player2, 7);
        harness.setHand(player1, player1Hand);
        harness.setHand(player2, player2Hand);

        addReadyMagus();
        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Magus of the Jar");
        harness.assertInGraveyard(player1, "Magus of the Jar");
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(player1Hand);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(player2Hand);
        assertThat(gd.exiledCards).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.exiledCards)
                .hasSize(player1Hand.size() + player2Hand.size())
                .allMatch(ExiledCardEntry::faceDown);
        harness.assertInGraveyard(player1, "Magus of the Jar");
    }

    @Test
    @DisplayName("The next end step discards current hands and returns the remembered cards")
    void nextEndStepReturnsRememberedCardsAfterDiscardingHands() {
        List<Card> player1Hand = List.of(new MagusOfTheJar(), new Forest());
        List<Card> player2Hand = List.of(new Forest(), new Island());
        setDeck(player1, 7);
        setDeck(player2, 7);
        harness.setHand(player1, player1Hand);
        harness.setHand(player2, player2Hand);

        addReadyMagus();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        List<Card> replacementHand1 = List.of(new Forest(), new Forest(), new Forest());
        List<Card> replacementHand2 = List.of(new Island(), new Island());
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
    @DisplayName("Empty hands still draw seven cards and exile nothing")
    void emptyHandsStillDrawSevenCardsAndExileNothing() {
        setDeck(player1, 7);
        setDeck(player2, 7);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        addReadyMagus();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.exiledCards).isEmpty();
        harness.assertInGraveyard(player1, "Magus of the Jar");
    }

    @Test
    @DisplayName("Empty original hands still create a delayed discard at the next end step")
    void emptyOriginalHandsStillDiscardDrawnCards() {
        setDeck(player1, 7);
        setDeck(player2, 7);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        addReadyMagus();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        List<Card> drawn1 = List.copyOf(gd.playerHands.get(player1.getId()));
        List<Card> drawn2 = List.copyOf(gd.playerHands.get(player2.getId()));
        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsAll(drawn1);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsAll(drawn2);
    }

    @Test
    @DisplayName("Activation during an end step waits until the following end step")
    void activationDuringEndStepWaitsForNextEndStep() {
        Card original1 = new Island();
        Card original2 = new Forest();
        setDeck(player1, 7);
        setDeck(player2, 7);
        harness.setHand(player1, List.of(original1));
        harness.setHand(player2, List.of(original2));
        addReadyMagus();
        harness.forceStep(TurnStep.END_STEP);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7).doesNotContain(original1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7).doesNotContain(original2);
        assertThat(gd.exiledCards).hasSize(2);

        advanceToEndStep(player2);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original1);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(original2);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Returning the exiled hands uses the stack and happens only once")
    void delayedReturnUsesStackAndTriggersOnlyOnce() {
        Card original1 = new Island();
        Card original2 = new Forest();
        setDeck(player1, 7);
        setDeck(player2, 7);
        harness.setHand(player1, List.of(original1));
        harness.setHand(player2, List.of(original2));
        addReadyMagus();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.exiledCards).hasSize(2);
        harness.passBothPriorities();

        advanceToEndStep(player2);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original1);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(original2);
        assertThat(gd.exiledCards).isEmpty();
    }

    private void addReadyMagus() {
        addCreatureReady(player1, new MagusOfTheJar());
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
            deck.add(new Forest());
        }
        harness.setLibrary(player, deck);
    }
}
