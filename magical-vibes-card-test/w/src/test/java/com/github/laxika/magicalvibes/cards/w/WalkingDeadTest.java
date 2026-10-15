package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WalkingDead.class)
class WalkingDeadTest extends BaseCardTest {

    @Test
    void activatingRegenerationAbilityTargetsWalkingDeadAndConsumesMana() {
        Permanent walkingDead = addCreatureReady(player1, new WalkingDead());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(walkingDead.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(walkingDead.isTapped()).isFalse();
    }

    @Test
    void resolvingRegenerationAbilityGrantsShield() {
        Permanent walkingDead = addCreatureReady(player1, new WalkingDead());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(walkingDead.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void regenerationShieldPreventsLethalDamageAndIsSpent() {
        Permanent walkingDead = addCreatureReady(player1, new WalkingDead());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        walkingDead.setMarkedDamage(1);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(walkingDead);
        assertThat(walkingDead.isTapped()).isTrue();
        assertThat(walkingDead.getRegenerationShield()).isZero();
        assertThat(walkingDead.getMarkedDamage()).isZero();
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent walkingDead = harness.addToBattlefieldAndReturn(player1, new WalkingDead());
        walkingDead.setSummoningSick(true);
        walkingDead.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(walkingDead.getRegenerationShield()).isEqualTo(1);
        assertThat(walkingDead.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void separateActivationsProtectAgainstSeparateDestructionEvents() {
        Permanent walkingDead = addCreatureReady(player1, new WalkingDead());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        walkingDead.setMarkedDamage(1);
        harness.runStateBasedActions();
        assertThat(walkingDead.getRegenerationShield()).isEqualTo(1);
        assertThat(walkingDead.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(walkingDead);

        walkingDead.setMarkedDamage(1);
        harness.runStateBasedActions();
        assertThat(walkingDead.getRegenerationShield()).isZero();
        assertThat(walkingDead.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(walkingDead);

        walkingDead.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Walking Dead");
        harness.assertInGraveyard(player1, "Walking Dead");
    }

    @Test
    void shieldCreationDoesNotRemoveAttackerFromCombatButRegenerationDoes() {
        Permanent walkingDead = addCreatureReady(player1, new WalkingDead());
        walkingDead.setAttacking(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(walkingDead.isAttacking()).isTrue();
        assertThat(walkingDead.isTapped()).isFalse();

        walkingDead.setMarkedDamage(1);
        harness.runStateBasedActions();

        assertThat(walkingDead.isAttacking()).isFalse();
        assertThat(walkingDead.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(walkingDead);
    }
}
