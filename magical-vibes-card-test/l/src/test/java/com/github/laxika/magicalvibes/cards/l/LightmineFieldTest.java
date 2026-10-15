package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.m.MakindiGriffin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightmineField.class, MakindiGriffin.class})
class LightmineFieldTest extends BaseCardTest {

    private Permanent addAttacker() {
        return addCreatureReady(player2, new MakindiGriffin());
    }

    @Test
    @DisplayName("Triggers once and deals damage equal to the number of attackers to each attacker")
    void damagesEachAttackerByAttackerCount() {
        harness.addToBattlefield(player1, new LightmineField());
        Permanent attacker1 = addAttacker();
        Permanent attacker2 = addAttacker();

        declareAttackers(player2, List.of(0, 1));

        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(attacker1.getMarkedDamage()).isEqualTo(2);
        assertThat(attacker2.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not damage creatures that did not attack")
    void ignoresCreaturesThatDidNotAttack() {
        harness.addToBattlefield(player1, new LightmineField());
        Permanent attacker = addAttacker();
        Permanent stayedHome = addAttacker();

        declareAttackers(player2, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
        assertThat(stayedHome.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An original attacker removed from combat still takes damage but is not counted")
    void damagesOriginalAttackerRemovedFromCombat() {
        harness.addToBattlefield(player1, new LightmineField());
        Permanent removed = addAttacker();
        Permanent remaining = addAttacker();

        declareAttackers(player2, List.of(0, 1));
        removed.setAttacking(false);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(removed.getMarkedDamage()).isEqualTo(1);
        assertThat(remaining.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature entering attacking increases damage but is not dealt damage")
    void doesNotDamageCreatureEnteringAttacking() {
        harness.addToBattlefield(player1, new LightmineField());
        Permanent declared = addAttacker();

        declareAttackers(player2, List.of(0));
        Permanent enteredAttacking = harness.addToBattlefieldAndReturn(player2, new MakindiGriffin());
        enteredAttacking.tap();
        enteredAttacking.setAttacking(true);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(declared.getMarkedDamage()).isEqualTo(2);
        assertThat(enteredAttacking.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Also triggers for its controller's attacking creatures")
    void damagesControllersAttackers() {
        harness.addToBattlefield(player1, new LightmineField());
        Permanent attacker = addCreatureReady(player1, new MakindiGriffin());

        declareAttackers(player1, List.of(1));
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }
}
