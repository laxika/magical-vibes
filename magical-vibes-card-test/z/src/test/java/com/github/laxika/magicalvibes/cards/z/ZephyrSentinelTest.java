package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.cards.t.ThirdPathSavant;
import com.github.laxika.magicalvibes.cards.w.WindingConstrictor;
import com.github.laxika.magicalvibes.cards.y.YotianFrontliner;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZephyrSentinel.class, YotianFrontliner.class, ThirdPathSavant.class, Disfigure.class,
        MaskwoodNexus.class, WindingConstrictor.class})
class ZephyrSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Returns another Soldier and gets a +1/+1 counter")
    void returnsSoldierAndGetsCounter() {
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new YotianFrontliner());
        harness.setHand(player1, List.of(new ZephyrSentinel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 0, soldier.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Yotian Frontliner");
        Permanent sentinel = findPermanent(player1, "Zephyr Sentinel");
        assertThat(sentinel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns a non-Soldier without putting a counter on itself")
    void returnsNonSoldierWithoutCounter() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ThirdPathSavant());
        harness.setHand(player1, List.of(new ZephyrSentinel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Third Path Savant");
        Permanent sentinel = findPermanent(player1, "Zephyr Sentinel");
        assertThat(sentinel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Can decline the optional target")
    void canDeclineTarget() {
        harness.castFromHand(player1, new ZephyrSentinel(), "{1}{U}");

        resolveAllTriggers();

        Permanent sentinel = findPermanent(player1, "Zephyr Sentinel");
        assertThat(sentinel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ThirdPathSavant());
        harness.setHand(player1, List.of(new ZephyrSentinel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }

    @Test
    void canDeclineTargetWithAnotherSoldierAvailable() {
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new YotianFrontliner());
        harness.castFromHand(player1, new ZephyrSentinel(), "{1}{U}");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(soldier);
        assertThat(findPermanent(player1, "Zephyr Sentinel")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInHand(player1, "Yotian Frontliner");
    }

    @Test
    void doesNotGetCounterWhenTargetLeavesBeforeResolution() {
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new YotianFrontliner());
        harness.setHand(player1, List.of(new ZephyrSentinel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, 0, soldier.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Disfigure()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player2, 0, soldier.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Yotian Frontliner");
        harness.assertNotInHand(player1, "Yotian Frontliner");
        assertThat(findPermanent(player1, "Zephyr Sentinel")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void stillReturnsSoldierWhenSentinelLeavesBeforeResolution() {
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new YotianFrontliner());
        harness.setHand(player1, List.of(new ZephyrSentinel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, 0, soldier.getId());
        harness.passBothPriorities();
        Permanent sentinel = findPermanent(player1, "Zephyr Sentinel");

        harness.setHand(player2, List.of(new Disfigure()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player2, 0, sentinel.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Zephyr Sentinel");
        harness.assertInHand(player1, "Yotian Frontliner");
        harness.assertNotOnBattlefield(player1, "Yotian Frontliner");
    }

    @Test
    void getsCounterWhenUnearthedSoldierIsExiledInsteadOfReturned() {
        YotianFrontliner frontliner = new YotianFrontliner();
        harness.setGraveyard(player1, List.of(frontliner));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent soldier = findPermanent(player1, "Yotian Frontliner");

        harness.setHand(player1, List.of(new ZephyrSentinel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, 0, soldier.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Yotian Frontliner");
        harness.assertNotInHand(player1, "Yotian Frontliner");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(frontliner);
        assertThat(findPermanent(player1, "Zephyr Sentinel")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void returnsSoldierBeforeApplyingCounterReplacementEffects() {
        harness.addToBattlefield(player1, new MaskwoodNexus());
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new WindingConstrictor());
        harness.setHand(player1, List.of(new ZephyrSentinel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, 0, soldier.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Winding Constrictor");
        harness.assertNotOnBattlefield(player1, "Winding Constrictor");
        assertThat(findPermanent(player1, "Zephyr Sentinel")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
