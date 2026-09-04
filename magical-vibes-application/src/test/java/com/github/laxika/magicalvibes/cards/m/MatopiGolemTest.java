package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarkPrivilege;
import com.github.laxika.magicalvibes.cards.p.PantherWarriors;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarkPrivilege.class, MatopiGolem.class, PantherWarriors.class})
class MatopiGolemTest extends BaseCardTest {

    private Permanent addGolemReady() {
        addCreatureReady(player1, new MatopiGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        return findPermanent(player1, "Matopi Golem");
    }

    /** Puts a shielded Golem in front of a lethal attacker so combat damage forces regeneration. */
    private void blockPantherWarriors(Permanent golem) {
        golem.setBlocking(true);
        golem.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new PantherWarriors());
        attacker.setAttacking(true);
        resolveCombat(player2);
    }

    private void addForeignShield(Permanent golem) {
        Permanent fodder = addCreatureReady(player1, new PantherWarriors());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DarkPrivilege());
        aura.setAttachedTo(golem.getId());

        harness.activateAbility(player1, 2, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
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

        blockPantherWarriors(findPermanent(player1, "Matopi Golem"));

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
        addForeignShield(golem);

        blockPantherWarriors(findPermanent(player1, "Matopi Golem"));

        harness.assertOnBattlefield(player1, "Matopi Golem");
        Permanent regenerated = findPermanent(player1, "Matopi Golem");
        assertThat(regenerated.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The controller can choose Matopi Golem's shield when a plain shield also applies")
    void canChooseMatopiShieldAmongMultipleShields() {
        Permanent golem = addGolemReady();

        addForeignShield(golem);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        blockPantherWarriors(golem);

        Permanent regenerated = findPermanent(player1, "Matopi Golem");
        assertThat(regenerated.getRegenerationShield()).isEqualTo(1);
        assertThat(regenerated.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Without a shield the Golem dies to lethal combat damage")
    void diesWithoutShield() {
        Permanent golem = addGolemReady();

        blockPantherWarriors(golem);

        harness.assertNotOnBattlefield(player1, "Matopi Golem");
        harness.assertInGraveyard(player1, "Matopi Golem");
    }
}
