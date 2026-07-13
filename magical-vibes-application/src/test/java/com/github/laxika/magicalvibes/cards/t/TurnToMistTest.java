package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.Quicksand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TurnToMistTest extends BaseCardTest {

    private void addTurnToMistMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Exiles the target creature and schedules its return")
    void exilesTargetCreature() {
        harness.setHand(player1, java.util.List.of(new TurnToMist()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        addTurnToMistMana();

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getDelayedActions(PendingExileReturn.class))
                .anyMatch(per -> per.card().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiled creature returns at the next end step under its owner's control")
    void returnsAtEndStep() {
        harness.setHand(player1, java.util.List.of(new TurnToMist()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        addTurnToMistMana();

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.setHand(player1, java.util.List.of(new TurnToMist()));
        harness.addToBattlefield(player2, new Quicksand());
        addTurnToMistMana();

        UUID quicksandId = harness.getPermanentId(player2, "Quicksand");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, quicksandId))
                .isInstanceOf(IllegalStateException.class);
    }
}
