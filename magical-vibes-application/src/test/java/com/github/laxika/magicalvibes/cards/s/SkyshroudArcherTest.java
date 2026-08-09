package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkyshroudArcherTest extends BaseCardTest {

    @Test
    @DisplayName("Taps to give a target creature with flying -1/-1 until end of turn")
    void givesFlyingCreatureMinusOneMinusOne() {
        addReadyArcher(player1);
        Permanent drake = addCreatureReady(player2, new WindDrake());

        harness.activateAbility(player1, 0, null, drake.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).get(0).isTapped()).isTrue();
    }

    @Test
    @DisplayName("-1/-1 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        addReadyArcher(player1);
        Permanent drake = addCreatureReady(player2, new WindDrake());

        harness.activateAbility(player1, 0, null, drake.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        addReadyArcher(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-1/-1 can kill a small flying creature")
    void canKillSmallFlyingCreature() {
        addReadyArcher(player1);
        Permanent hawk = addCreatureReady(player2, new SuntailHawk());

        harness.activateAbility(player1, 0, null, hawk.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(hawk.getId()));
    }

    private Permanent addReadyArcher(Player player) {
        Permanent perm = new Permanent(new SkyshroudArcher());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
