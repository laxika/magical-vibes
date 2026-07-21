package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccursedHordeTest extends BaseCardTest {

    private void addHorde() {
        Permanent horde = harness.addToBattlefieldAndReturn(player1, new AccursedHorde());
        horde.setSummoningSick(false);
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Grants indestructible to an attacking Zombie, then it wears off at end of turn")
    void grantsIndestructibleToAttackingZombie() {
        addHorde();
        Permanent attackingZombie = harness.addToBattlefieldAndReturn(player1, new AccursedHorde());
        attackingZombie.setSummoningSick(false);
        attackingZombie.setAttacking(true);
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, attackingZombie.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attackingZombie, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attackingZombie, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a Zombie that is not attacking")
    void cannotTargetNonAttackingZombie() {
        addHorde();
        Permanent idleZombie = harness.addToBattlefieldAndReturn(player1, new AccursedHorde());
        idleZombie.setSummoningSick(false);
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, idleZombie.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an attacking non-Zombie creature")
    void cannotTargetAttackingNonZombie() {
        addHorde();
        Permanent attackingBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attackingBear.setSummoningSick(false);
        attackingBear.setAttacking(true);
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, attackingBear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
