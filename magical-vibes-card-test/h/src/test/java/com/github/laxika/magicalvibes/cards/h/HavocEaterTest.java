package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HavocEater.class})
class HavocEaterTest extends BaseCardTest {

    @Test
    void goadsAnOpponentCreatureAndAddsItsPowerAsCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HavocEater());
        harness.setHand(player1, List.of(new HavocEater()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent havocEater = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Havoc Eater"));
        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
        assertThat(havocEater.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void mayDeclineEachOpponentTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HavocEater());
        harness.setHand(player1, List.of(new HavocEater()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        Permanent havocEater = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Havoc Eater"));
        assertThat(als.getMustAttackRequirementCount(gd, target)).isZero();
        assertThat(havocEater.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void usesTargetPowerWhenTheAbilityResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HavocEater());
        Permanent havocEater = harness.enterBattlefieldAndReturn(player1, new HavocEater());
        harness.handlePermanentChosen(player1, target.getId());

        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.passBothPriorities();

        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
        assertThat(havocEater.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    void anIllegalSoleTargetPreventsGoadAndCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HavocEater());
        Permanent havocEater = harness.enterBattlefieldAndReturn(player1, new HavocEater());
        harness.handlePermanentChosen(player1, target.getId());

        target.setCounterCount(CounterType.HEXPROOF, 1);
        harness.passBothPriorities();

        assertThat(als.getMustAttackRequirementCount(gd, target)).isZero();
        assertThat(havocEater.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void stillGoadsIfHavocEaterLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HavocEater());
        Permanent havocEater = harness.enterBattlefieldAndReturn(player1, new HavocEater());
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(havocEater);
        harness.passBothPriorities();

        assertThat(als.getMustAttackRequirementCount(gd, target)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Havoc Eater");
    }

    @Test
    void entersWithoutCountersWhenOpponentsHaveNoCreatures() {
        Permanent havocEater = harness.enterBattlefieldAndReturn(player1, new HavocEater());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Havoc Eater");
        assertThat(havocEater.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
