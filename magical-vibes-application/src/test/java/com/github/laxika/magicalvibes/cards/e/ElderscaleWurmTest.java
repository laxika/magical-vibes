package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SignInBlood;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElderscaleWurm.class, HillGiant.class, LavaAxe.class, Shock.class,
        SignInBlood.class, TurnToFrog.class})
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

        harness.castAndResolveSorcery(player2, 0, player1.getId());

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
        harness.castFromHand(player1, new ElderscaleWurm(), "{4}{G}{G}{G}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
    }

    private void shockPlayer1() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
    }

    @Test
    @DisplayName("Entering at exactly 7 life does not trigger the life-setting ability")
    void enteringAtSevenDoesNotTrigger() {
        harness.setLife(player1, 7);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ElderscaleWurm(), "{4}{G}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 7);
    }

    @Test
    @DisplayName("Entering above 7 life does not trigger the life-setting ability")
    void enteringAboveSevenDoesNotTrigger() {
        harness.setLife(player1, 12);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ElderscaleWurm(), "{4}{G}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("The enter trigger does nothing if life rises above 7 before resolution")
    void enterTriggerRechecksLifeOnResolution() {
        harness.setLife(player1, 3);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ElderscaleWurm(), "{4}{G}{G}{G}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.setLife(player1, 8);

        harness.passBothPriorities();

        harness.assertLife(player1, 8);
    }

    @Test
    @DisplayName("Losing all abilities removes the Wurm's damage floor")
    void losingAbilitiesRemovesDamageFloor() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new ElderscaleWurm());
        harness.setLife(player1, 8);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, wurm.getId());

        shockPlayer1();

        harness.assertLife(player1, 6);
    }

    @Test
    @DisplayName("Life loss bypasses the damage floor and disables it below 7")
    void lifeLossBypassesFloor() {
        harness.addToBattlefield(player1, new ElderscaleWurm());
        harness.setLife(player1, 7);
        harness.setLibrary(player1, List.of(new ElderscaleWurm(), new ElderscaleWurm()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new SignInBlood()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.assertLife(player1, 5);

        shockPlayer1();

        harness.assertLife(player1, 3);
    }
}
