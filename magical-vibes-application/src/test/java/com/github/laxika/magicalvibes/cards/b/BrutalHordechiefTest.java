package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrutalHordechiefTest extends BaseCardTest {

    @Test
    @DisplayName("Each attacking creature makes the defending player lose 1 life and its controller gain 1 life")
    void attackTriggerDrainsDefendingPlayer() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        addCreatureReady(player1, new BrutalHordechief());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(8);
    }

    @Test
    @DisplayName("Activated ability makes the controller choose how opposing creatures block")
    void activatedAbilityGivesControllerBlockChoice() {
        addCreatureReady(player1, new BrutalHordechief());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(1));
        resolveAllTriggers();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        PendingInteraction.BlockerDeclaration pending =
                gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class);
        assertThat(pending).isNotNull();
        assertThat(pending.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(pending.defenderId()).isEqualTo(player2.getId());
        assertThat(pending.choosingForOpponent()).isTrue();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
