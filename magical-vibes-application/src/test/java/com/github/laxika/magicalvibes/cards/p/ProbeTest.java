package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Probe.class, Island.class, Forest.class, Swamp.class})
class ProbeTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, draws three and discards two from its controller")
    void drawsThreeAndDiscardsTwoWithoutKicker() {
        harness.setHand(player1, List.of(new Probe(), new Island(), new Forest()));
        harness.setHand(player2, List.of(new Island(), new Forest(), new Swamp()));
        harness.setLibrary(player1, List.of(new Swamp(), new Island(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("With kicker, makes the target player discard two after the controller discards two")
    void kickedProbeMakesTargetPlayerDiscardTwo() {
        harness.setHand(player1, List.of(new Probe(), new Island(), new Forest()));
        harness.setHand(player2, List.of(new Island(), new Forest(), new Swamp(), new Island()));
        harness.setLibrary(player1, List.of(new Swamp(), new Island(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        gs.playCard(gd, player1, 0, 0, player2.getId(), null, List.of(), List.of(),
                false, null, null, List.of(), null, List.of(), true);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Rejects a permanent as Probe's kicked player target")
    void rejectsPermanentTarget() {
        harness.addToBattlefield(player2, new Island());
        UUID permanentId = harness.getPermanentId(player2, "Island");
        harness.setHand(player1, List.of(new Probe()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, permanentId, null, List.of(), List.of(),
                false, null, null, List.of(), null, List.of(), true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only target players");
    }

    @Test
    @DisplayName("Kicker requires a target player")
    void kickerRequiresTargetPlayer() {
        harness.setHand(player1, List.of(new Probe()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castKickedSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("target");
    }

    @Test
    @DisplayName("Without kicker, Probe does not accept a target player")
    void unKickedProbeDoesNotAcceptTargetPlayer() {
        harness.setHand(player1, List.of(new Probe()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("target");
    }
}
