package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CybermanPatrol;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFifthDoctor.class, CybermanPatrol.class})
class TheFifthDoctorTest extends BaseCardTest {

    @Test
    @DisplayName("At your end step, peaceful creatures get a counter and untap")
    void rewardsCreaturesThatStayedOutOfCombat() {
        Permanent doctor = harness.enterBattlefieldAndReturn(player1, new TheFifthDoctor());
        Permanent peaceful = addCreatureReady(player1, new CybermanPatrol());
        peaceful.tap();
        Permanent attacker = addCreatureReady(player1, new CybermanPatrol());
        attacker.tap();
        attacker.setAttackedThisTurn(true);
        Permanent entered = harness.enterBattlefieldAndReturn(player1, new CybermanPatrol());
        entered.tap();
        Permanent opponentCreature = addCreatureReady(player2, new CybermanPatrol());
        opponentCreature.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(peaceful.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(peaceful.isTapped()).isFalse();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(attacker.isTapped()).isTrue();
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(entered.isTapped()).isTrue();
        assertThat(doctor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An established Doctor and untapped peaceful creatures receive counters")
    void rewardsItselfAndUntappedCreatures() {
        Permanent doctor = addCreatureReady(player1, new TheFifthDoctor());
        doctor.tap();
        Permanent peaceful = addCreatureReady(player1, new CybermanPatrol());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(doctor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(doctor.isTapped()).isFalse();
        assertThat(peaceful.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(peaceful.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Peaceful Coexistence does not trigger during an opponent's end step")
    void doesNotRewardCreaturesOnOpponentsEndStep() {
        Permanent doctor = addCreatureReady(player1, new TheFifthDoctor());
        doctor.tap();
        Permanent peaceful = addCreatureReady(player1, new CybermanPatrol());
        peaceful.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(doctor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(doctor.isTapped()).isTrue();
        assertThat(peaceful.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(peaceful.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing the Doctor after its ability triggers does not stop the reward")
    void resolvesAfterDoctorLeavesBattlefield() {
        Permanent doctor = addCreatureReady(player1, new TheFifthDoctor());
        Permanent peaceful = addCreatureReady(player1, new CybermanPatrol());
        peaceful.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(doctor);
        gd.playerGraveyards.get(player1.getId()).add(doctor.getCard());
        resolveAllTriggers();

        assertThat(peaceful.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(peaceful.isTapped()).isFalse();
    }
}
