package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VitalityHunter.class, GrizzlyBears.class})
class VitalityHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrosity X puts lifelink counters on up to X target creatures")
    void monstrosityUsesPaidXForCountersAndLifelinkTargets() {
        Permanent hunter = addReadyHunter();
        Permanent ownBear = addReadyCreature(player1);
        Permanent opposingBear = addReadyCreature(player2);
        addMonstrosityMana(2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ownBear.getId());
        harness.handlePermanentChosen(player1, opposingBear.getId());
        harness.passBothPriorities();

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(hunter.isMonstrous()).isTrue();
        assertThat(ownBear.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(opposingBear.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.LIFELINK)).isTrue();
    }


    @Test
    @DisplayName("Monstrosity zero still makes the hunter monstrous")
    void zeroXBecomesMonstrousWithoutCounters() {
        Permanent hunter = addReadyHunter();
        addMonstrosityMana(0);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hunter.isMonstrous()).isTrue();
        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(hunter.getCounterCount(CounterType.LIFELINK)).isZero();
    }

    @Test
    @DisplayName("The controller may choose no lifelink targets for positive X")
    void mayDeclineAllTargets() {
        Permanent hunter = addReadyHunter();
        addMonstrosityMana(2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(hunter.isMonstrous()).isTrue();
        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(hunter.getCounterCount(CounterType.LIFELINK)).isZero();
    }

    @Test
    @DisplayName("The hunter may target itself and choose fewer than X creatures")
    void mayTargetItselfAndStopBeforeX() {
        Permanent hunter = addReadyHunter();
        addMonstrosityMana(2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, hunter.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(hunter.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
    }

    @Test
    @DisplayName("An already monstrous hunter may activate monstrosity again without effect")
    void mayActivateAgainAfterBecomingMonstrous() {
        Permanent hunter = addReadyHunter();
        addMonstrosityMana(1);
        harness.activateAbility(player1, 0, 1, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, hunter.getId());
        harness.passBothPriorities();
        addMonstrosityMana(2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(hunter.isMonstrous()).isTrue();
        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hunter.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Monstrosity X permits more than ninety-nine lifelink targets")
    void mayChooseOneHundredTargetsWhenXIsOneHundred() {
        Permanent hunter = addReadyHunter();
        java.util.List<Permanent> targets = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            targets.add(harness.addToBattlefieldAndReturn(player2, new VitalityHunter()));
        }
        addMonstrosityMana(100);

        harness.activateAbility(player1, 0, 100, null);
        harness.passBothPriorities();
        for (Permanent target : targets) {
            harness.handlePermanentChosen(player1, target.getId());
        }
        harness.passBothPriorities();

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(100);
        assertThat(targets).allSatisfy(target ->
                assertThat(target.getCounterCount(CounterType.LIFELINK)).isEqualTo(1));
    }

    private Permanent addReadyHunter() {
        Permanent hunter = harness.addToBattlefieldAndReturn(player1, new VitalityHunter());
        hunter.setSummoningSick(false);
        return hunter;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private void addMonstrosityMana(int x) {
        harness.addMana(player1, ManaColor.COLORLESS, x);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
