package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VampireOfTheDireMoon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SorinVampireLordTest extends BaseCardTest {

    @Test
    @DisplayName("+1 gives up to one target creature +2/+0 until end of turn")
    void plusOneBoostsTargetCreature() {
        Permanent sorin = addReadySorin(4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-2 deals 4 damage to a player and gains 4 life")
    void minusTwoDealsDamageAndGainsLife() {
        Permanent sorin = addReadySorin(4);
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-8 gives Vampires a tap ability that permanently gains control of a creature")
    void minusEightGrantsVampireControlAbility() {
        Permanent sorin = addReadySorin(8);
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VampireOfTheDireMoon());
        vampire.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        int vampireIndex = gd.playerBattlefields.get(player1.getId()).indexOf(vampire);
        harness.activateAbility(player1, vampireIndex, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    @DisplayName("-8's granted ability only targets creatures and expires at end of turn")
    void minusEightGrantIsTargetedAndTemporary() {
        addReadySorin(8);
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VampireOfTheDireMoon());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new com.github.laxika.magicalvibes.cards.m.Mountain());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        int vampireIndex = gd.playerBattlefields.get(player1.getId()).indexOf(vampire);
        assertThatThrownBy(() -> harness.activateAbility(player1, vampireIndex, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        vampire.untap();

        assertThatThrownBy(() -> harness.activateAbility(player1, vampireIndex, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySorin(int loyalty) {
        Permanent perm = new Permanent(new SorinVampireLord());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
