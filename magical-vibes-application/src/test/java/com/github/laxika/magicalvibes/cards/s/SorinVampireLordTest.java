package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.v.VampireOfTheDireMoon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SorinVampireLord.class, GreenwoodSentinel.class, VampireOfTheDireMoon.class, Mountain.class})
class SorinVampireLordTest extends BaseCardTest {

    @Test
    @DisplayName("+1 gives up to one target creature +2/+0 until end of turn")
    void plusOneBoostsTargetCreature() {
        Permanent sorin = addReadySorin(4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

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
        vampire.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        int vampireIndex = gd.playerBattlefields.get(player1.getId()).indexOf(vampire);
        assertThatThrownBy(() -> harness.activateAbility(player1, vampireIndex, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        vampire.untap();

        assertThatThrownBy(() -> harness.activateAbility(player1, vampireIndex, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void plusOneCanResolveWithoutATarget() {
        Permanent sorin = addReadySorin(4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void plusOneBoostExpiresAtCleanup() {
        addReadySorin(4);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void minusTwoDestroysCreatureAndGainsLife() {
        addReadySorin(4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.setLife(player1, 15);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        harness.assertLife(player1, 19);
    }

    @Test
    void minusTwoDoesNotGainLifeWhenItsTargetLeaves() {
        Permanent sorin = addReadySorin(4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.setLife(player1, 15);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void minusEightOnlyGrantsToVampiresControlledAtResolution() {
        addReadySorin(8);
        Permanent nonVampire = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent opposingVampire = harness.addToBattlefieldAndReturn(player2, new VampireOfTheDireMoon());
        nonVampire.setSummoningSick(false);
        opposingVampire.setSummoningSick(false);

        harness.activateAbility(player1, 0, 2, null, null);
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VampireOfTheDireMoon());
        vampire.setSummoningSick(false);
        harness.passBothPriorities();
        Permanent lateVampire = harness.addToBattlefieldAndReturn(player1, new VampireOfTheDireMoon());
        lateVampire.setSummoningSick(false);

        int nonVampireIndex = gd.playerBattlefields.get(player1.getId()).indexOf(nonVampire);
        int lateVampireIndex = gd.playerBattlefields.get(player1.getId()).indexOf(lateVampire);
        assertThatThrownBy(() -> harness.activateAbility(player1, nonVampireIndex, 0, null, opposingVampire.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, lateVampireIndex, 0, null, opposingVampire.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, nonVampire.getId()))
                .isInstanceOf(IllegalStateException.class);

        int vampireIndex = gd.playerBattlefields.get(player1.getId()).indexOf(vampire);
        harness.activateAbility(player1, vampireIndex, 0, null, opposingVampire.getId());
        harness.passBothPriorities();

        assertThat(vampire.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opposingVampire);
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opposingVampire);
    }

    private Permanent addReadySorin(int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new SorinVampireLord());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
