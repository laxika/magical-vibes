package com.github.laxika.magicalvibes.cards.u;

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

class UnravelingMummyTest extends BaseCardTest {

    private Permanent addMummy() {
        Permanent mummy = harness.addToBattlefieldAndReturn(player1, new UnravelingMummy());
        mummy.setSummoningSick(false);
        return mummy;
    }

    private void addWhiteMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addBlackMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Grants lifelink to an attacking Zombie, then it wears off at end of turn")
    void grantsLifelinkToAttackingZombie() {
        addMummy();
        Permanent attackingZombie = harness.addToBattlefieldAndReturn(player1, new UnravelingMummy());
        attackingZombie.setSummoningSick(false);
        attackingZombie.setAttacking(true);
        addWhiteMana();

        harness.activateAbility(player1, 0, 0, null, attackingZombie.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attackingZombie, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attackingZombie, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Grants deathtouch to an attacking Zombie, then it wears off at end of turn")
    void grantsDeathtouchToAttackingZombie() {
        addMummy();
        Permanent attackingZombie = harness.addToBattlefieldAndReturn(player1, new UnravelingMummy());
        attackingZombie.setSummoningSick(false);
        attackingZombie.setAttacking(true);
        addBlackMana();

        harness.activateAbility(player1, 0, 1, null, attackingZombie.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attackingZombie, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attackingZombie, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a Zombie that is not attacking")
    void cannotTargetNonAttackingZombie() {
        addMummy();
        Permanent idleZombie = harness.addToBattlefieldAndReturn(player1, new UnravelingMummy());
        idleZombie.setSummoningSick(false);
        addWhiteMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, idleZombie.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an attacking non-Zombie creature")
    void cannotTargetAttackingNonZombie() {
        addMummy();
        Permanent attackingBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attackingBear.setSummoningSick(false);
        attackingBear.setAttacking(true);
        addBlackMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, attackingBear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
