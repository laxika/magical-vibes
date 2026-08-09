package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JalumTome;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThirstForKnowledgeTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards and allows one artifact discard")
    void drawsThreeAndCanDiscardOneArtifact() {
        setDeck(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.setHand(player1, List.of(new ThirstForKnowledge(), new GrizzlyBears(), new JalumTome()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);

        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May choose a second card after discarding an artifact")
    void mayChooseTwoCardsIncludingAnArtifact() {
        setDeck(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.setHand(player1, List.of(new ThirstForKnowledge(), new JalumTome(), new JalumTome()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Requires two discards when no artifact is discarded")
    void requiresTwoDiscardsWithoutArtifact() {
        setDeck(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.setHand(player1, List.of(new ThirstForKnowledge(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
