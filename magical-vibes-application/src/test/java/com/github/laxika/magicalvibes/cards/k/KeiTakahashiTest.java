package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KeiTakahashiTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next 2 damage dealt to the target creature")
    void preventsNextTwoDamageToTargetCreature() {
        addReadyKei();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        activateKei(target);

        addReadyPyromancer(player2);
        addReadyPyromancer(player2);
        addReadyPyromancer(player2);

        dealDamage(player2, 0, target);
        dealDamage(player2, 1, target);
        assertThat(target.getMarkedDamage()).isZero();

        dealDamage(player2, 2, target);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage prevention expires at end of turn")
    void preventionExpiresAtEndOfTurn() {
        addReadyKei();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        activateKei(target);
        addReadyPyromancer(player2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        dealDamage(player2, 0, target);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        addReadyKei();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyKei() {
        return addCreatureReady(player1, new KeiTakahashi());
    }

    private Permanent addReadyPyromancer(Player player) {
        return addCreatureReady(player, new ProdigalPyromancer());
    }

    private void activateKei(Permanent target) {
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }

    private void dealDamage(Player player, int permanentIndex, Permanent target) {
        harness.activateAbility(player, permanentIndex, null, target.getId());
        harness.passBothPriorities();
    }
}
