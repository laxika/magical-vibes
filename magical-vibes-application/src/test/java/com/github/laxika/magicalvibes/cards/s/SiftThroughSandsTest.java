package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PeerThroughDepths;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SiftThroughSandsTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards, then discards a card")
    void drawsTwoThenDiscardsOne() {
        setDeck(player1, List.of(new Forest(), new Forest(), new Forest()));

        castSift(); // castSift sets the hand to the spell alone, so the hand is empty on resolution

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Sift Through Sands");
    }

    @Test
    @DisplayName("Without both named spells cast this turn there is no search")
    void noSearchWithoutBothNamedSpells() {
        setDeck(player1, List.of(new Forest(), new Forest(), new Forest()));
        castReachThroughMists();

        setDeck(player1, List.of(new Forest(), new Forest()));
        castSift();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With both named spells cast this turn the controller may search for The Unspeakable")
    void offersSearchAfterBothNamedSpells() {
        gd.playerDecks.get(player1.getId()).clear();
        castPeerThroughDepths(); // empty library — resolves with no interaction

        setDeck(player1, List.of(new Forest(), new Forest(), new Forest()));
        castReachThroughMists();

        setDeck(player1, List.of(new Forest(), new Forest()));
        castSift();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castSift() {
        harness.setHand(player1, List.of(new SiftThroughSands()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void castReachThroughMists() {
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void castPeerThroughDepths() {
        harness.setHand(player1, List.of(new PeerThroughDepths()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
