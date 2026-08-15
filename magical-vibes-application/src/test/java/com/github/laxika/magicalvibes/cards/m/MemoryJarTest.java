package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryJarTest extends BaseCardTest {

    @Test
    @DisplayName("Activation exiles both hands face down and gives each player seven cards")
    void activationExilesHandsAndDrawsSeven() {
        List<Card> player1Hand = List.of(new MemoryJar(), new LlanowarElves());
        List<Card> player2Hand = List.of(new Forest(), new Island(), new Forest());
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
        List<Card> player1Hand = List.of(new MemoryJar(), new LlanowarElves());
        List<Card> player2Hand = List.of(new Forest(), new Island());
        setDeck(player1, 7);
        setDeck(player2, 7);
        harness.setHand(player1, player1Hand);
        harness.setHand(player2, player2Hand);

        addReadyJar();
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

    private void addReadyJar() {
        Permanent jar = harness.addToBattlefieldAndReturn(player1, new MemoryJar());
        jar.setSummoningSick(false);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setDeck(Player player, int count) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            deck.add(new Forest());
        }
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(deck);
    }
}
