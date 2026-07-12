package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.Quicksand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MistmeadowWitchTest extends BaseCardTest {

    private void addReadyWitch(Player player) {
        Permanent perm = new Permanent(new MistmeadowWitch());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
    }

    private void addWitchMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Ability exiles the target creature and schedules its return")
    void exilesTargetCreature() {
        addReadyWitch(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        addWitchMana(player1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, bearsId);
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
        addReadyWitch(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        addWitchMana(player1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, bearsId);
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
        addReadyWitch(player1);
        harness.addToBattlefield(player2, new Quicksand());
        addWitchMana(player1);

        UUID quicksandId = harness.getPermanentId(player2, "Quicksand");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, quicksandId))
                .isInstanceOf(IllegalStateException.class);
    }
}
