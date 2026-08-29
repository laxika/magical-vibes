package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TezzeretsAmbitionTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards and prompts for a discard when no artifact is controlled")
    void drawsThreeAndDiscardsWithoutArtifact() {
        setDeck(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.setHand(player1, List.of(new TezzeretsAmbition()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Draws three cards without discarding when an artifact is controlled")
    void drawsThreeWithoutDiscardingWithArtifact() {
        setDeck(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.addToBattlefield(player1, new Spellbook());
        harness.setHand(player1, List.of(new TezzeretsAmbition()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
