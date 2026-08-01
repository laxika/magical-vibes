package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MatopiGolemTest extends BaseCardTest {

    private Permanent addGolemReady() {
        addCreatureReady(player1, new MatopiGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        return findPermanent(player1, "Matopi Golem");
    }

    /** Puts a shielded Golem in front of a 3/3 attacker so combat damage forces the regeneration. */
    private void blockHillGiant(Permanent golem) {
        golem.setBlocking(true);
        golem.addBlockingTarget(0);

        Permanent attacker = new Permanent(new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Activating the ability only grants a shield — no counter yet")
    void activationAlonePutsNoCounter() {
        Permanent golem = addGolemReady();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(golem.getRegenerationShield()).isEqualTo(1);
        assertThat(golem.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Spending the shield regenerates the Golem and puts a -1/-1 counter on it")
    void regeneratingPutsMinusOneCounter() {
        Permanent golem = addGolemReady();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        blockHillGiant(findPermanent(player1, "Matopi Golem"));

        harness.assertOnBattlefield(player1, "Matopi Golem");
        Permanent regenerated = findPermanent(player1, "Matopi Golem");
        assertThat(regenerated.isTapped()).isTrue();
        assertThat(regenerated.getRegenerationShield()).isZero();
        assertThat(regenerated.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A shield from another source regenerates without putting a counter")
    void foreignShieldPutsNoCounter() {
        Permanent golem = addGolemReady();
        golem.setRegenerationShield(1);

        blockHillGiant(findPermanent(player1, "Matopi Golem"));

        harness.assertOnBattlefield(player1, "Matopi Golem");
        Permanent regenerated = findPermanent(player1, "Matopi Golem");
        assertThat(regenerated.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Without a shield the Golem dies to lethal combat damage")
    void diesWithoutShield() {
        Permanent golem = addGolemReady();

        blockHillGiant(golem);

        harness.assertNotOnBattlefield(player1, "Matopi Golem");
        harness.assertInGraveyard(player1, "Matopi Golem");
    }
}
