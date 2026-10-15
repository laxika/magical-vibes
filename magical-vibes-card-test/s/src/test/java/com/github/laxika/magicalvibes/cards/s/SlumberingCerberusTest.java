package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlumberingCerberus.class})
class SlumberingCerberusTest extends BaseCardTest {

    @Test
    @DisplayName("Does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent cerberus = addReadyCerberus(player1);
        cerberus.tap();

        advanceToNextTurn(player2);

        assertThat(cerberus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps at the beginning of each end step when a creature died this turn")
    void untapsAtEndStepWhenCreatureDied() {
        Permanent cerberus = addReadyCerberus(player1);
        cerberus.tap();
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(cerberus.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger at the end step when no creature died")
    void staysTappedWithoutCreatureDeath() {
        Permanent cerberus = addReadyCerberus(player1);
        cerberus.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(cerberus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's creature dying enables the trigger on the opponent's end step")
    void untapsOnOpponentsEndStepAfterOpponentsCreatureDies() {
        Permanent cerberus = addReadyCerberus(player1);
        cerberus.tap();
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new SlumberingCerberus());
        victim.setMarkedDamage(2);
        harness.runStateBasedActions();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(victim.getCard());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(cerberus.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(cerberus.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A creature dying after the end step begins does not trigger the ability retroactively")
    void deathAfterEndStepBeginsDoesNotUntap() {
        Permanent cerberus = addReadyCerberus(player1);
        cerberus.tap();
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new SlumberingCerberus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();

        victim.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(victim.getCard());
        assertThat(gd.stack).isEmpty();
        assertThat(cerberus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A death on the previous turn does not enable the next turn's end-step trigger")
    void deathCountResetsForNextTurn() {
        Permanent cerberus = addReadyCerberus(player1);
        cerberus.tap();
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new SlumberingCerberus());
        victim.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(cerberus.isTapped()).isFalse();

        harness.passUntil(player2, TurnStep.UPKEEP);
        cerberus.tap();
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(cerberus.isTapped()).isTrue();
    }

    private Permanent addReadyCerberus(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SlumberingCerberus());
        perm.setSummoningSick(false);
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);
    }
}
