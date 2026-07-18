package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CursedRackTest extends BaseCardTest {

    @Test
    @DisplayName("Chosen opponent must discard down to four during cleanup")
    void opponentDiscardsDownToFour() {
        harness.addToBattlefield(player1, new CursedRack());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        // Opponent holds six cards; max hand size is set to four, so two must go.
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Mountain()
        )));

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.CLEANUP);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Chosen opponent holding exactly four cards need not discard")
    void noDiscardWhenOpponentAtFour() {
        harness.addToBattlefield(player1, new CursedRack());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new Forest(), new Mountain()
        )));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Controller's own hand size is unaffected (still seven)")
    void controllerHandSizeUnaffected() {
        harness.addToBattlefield(player1, new CursedRack());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        // Eight cards: a normal player discards down to seven, not four.
        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Forest(),
                new Mountain(), new Plains()
        )));

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.CLEANUP);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Hand-size limit is lifted when Cursed Rack leaves the battlefield")
    void handSizeRestoredWhenRemoved() {
        harness.addToBattlefield(player1, new CursedRack());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Mountain()
        )));

        // Remove Cursed Rack before cleanup — max hand size returns to seven.
        gd.playerBattlefields.get(player1.getId()).clear();

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(6);
    }
}
