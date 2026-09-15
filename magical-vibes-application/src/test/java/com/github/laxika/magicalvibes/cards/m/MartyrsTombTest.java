package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GoblinLegionnaire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MartyrsTomb.class, GoblinLegionnaire.class})
class MartyrsTombTest extends BaseCardTest {

    @Test
    @DisplayName("Pays 2 life and prevents the next 1 damage to a target creature")
    void paysLifeAndPreventsNextDamageToCreature() {
        harness.addToBattlefield(player1, new MartyrsTomb());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinLegionnaire());
        Permanent damageSource = harness.addToBattlefieldAndReturn(player2, new GoblinLegionnaire());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(target.getDamagePreventionShield()).isEqualTo(1);

        harness.addMana(player2, ManaColor.RED, 1);
        int sourceIndex = gd.playerBattlefields.get(player2.getId()).indexOf(damageSource);
        harness.activateAbility(player2, sourceIndex, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Prevention shield expires at the end of the turn")
    void preventionShieldExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new MartyrsTomb());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinLegionnaire());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new MartyrsTomb());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
