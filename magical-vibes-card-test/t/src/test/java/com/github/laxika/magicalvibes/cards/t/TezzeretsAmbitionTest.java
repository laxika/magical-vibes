package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TezzeretsAmbition.class, Forest.class, Island.class, Mountain.class, PropheticPrism.class})
class TezzeretsAmbitionTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards and prompts for a discard when no artifact is controlled")
    void drawsThreeAndDiscardsWithoutArtifact() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.setHand(player1, List.of(new TezzeretsAmbition()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Draws three cards without discarding when an artifact is controlled")
    void drawsThreeWithoutDiscardingWithArtifact() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.setHand(player1, List.of(new TezzeretsAmbition()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's artifact does not prevent the discard")
    void opponentArtifactDoesNotPreventDiscard() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.setHand(player1, List.of(new TezzeretsAmbition()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An artifact in hand does not prevent discarding and may itself be discarded")
    void mayDiscardCardAlreadyInHand() {
        PropheticPrism prism = new PropheticPrism();
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.setHand(player1, List.of(new TezzeretsAmbition(), prism));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3).doesNotContain(prism);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(prism);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An artifact added after casting prevents discarding at resolution")
    void checksArtifactsAtResolution() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.setHand(player1, List.of(new TezzeretsAmbition()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
