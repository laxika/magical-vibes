package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElderscaleWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Entering with less than 7 life sets the controller's life total to 7")
    void enterBelowSevenSetsLifeToSeven() {
        harness.setLife(player1, 3);

        castElderscaleWurm();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(7);
    }

    @Test
    @DisplayName("Entering with 7 or more life leaves the controller's life total alone")
    void enterAtOrAboveSevenLeavesLifeAlone() {
        harness.setLife(player1, 12);

        castElderscaleWurm();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Noncombat damage can't reduce the controller's life total below 7")
    void noncombatDamageCappedAtSeven() {
        harness.addToBattlefield(player1, new ElderscaleWurm());
        harness.setLife(player1, 8);

        shockPlayer1();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(7);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Large noncombat damage is still capped at 7 while the controller has 7 or more life")
    void largeDamageCappedAtSeven() {
        harness.addToBattlefield(player1, new ElderscaleWurm());
        harness.setLife(player1, 7);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LavaAxe()));
        harness.addMana(player2, ManaColor.RED, 5);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(7);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("The floor does not apply once the controller is already below 7 life")
    void noFloorWhenAlreadyBelowSeven() {
        harness.addToBattlefield(player1, new ElderscaleWurm());
        harness.setLife(player1, 6);

        shockPlayer1();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(4);
    }

    @Test
    @DisplayName("Combat damage can't reduce the controller's life total below 7")
    void combatDamageCappedAtSeven() {
        harness.addToBattlefield(player1, new ElderscaleWurm());
        harness.setLife(player1, 8);

        Permanent attacker = addCreatureReady(player2, new HillGiant());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(7);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    private void castElderscaleWurm() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ElderscaleWurm()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
    }

    private void shockPlayer1() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
    }
}
