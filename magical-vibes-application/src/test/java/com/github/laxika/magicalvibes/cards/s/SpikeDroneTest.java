package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpikeDrone.class, CanopySpider.class, Forest.class})
class SpikeDroneTest extends BaseCardTest {

    private Permanent castDrone() {
        harness.castFromHand(player1, new SpikeDrone(), "{G}");
        harness.passBothPriorities();
        return findPermanent(player1, "Spike Drone");
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    @Test
    @DisplayName("Enters with a +1/+1 counter, making it a 1/1")
    void entersWithCounter() {
        Permanent drone = castDrone();

        assertThat(drone.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, drone)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drone)).isEqualTo(1);
    }

    @Test
    @DisplayName("Moves its +1/+1 counter to target creature")
    void movesCounterToTargetCreature() {
        Permanent drone = castDrone();
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new CanopySpider());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(drone), 0, null, spider.getId());
        harness.passBothPriorities();

        assertThat(drone.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(4);
    }

    @Test
    @DisplayName("Can put a +1/+1 counter on a creature an opponent controls")
    void canTargetOpponentCreature() {
        Permanent drone = castDrone();
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new CanopySpider());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(drone), 0, null, spider.getId());
        harness.passBothPriorities();

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot be activated without a +1/+1 counter to remove")
    void cannotActivateWithoutCounter() {
        Permanent drone = harness.enterBattlefieldAndReturn(player1, new SpikeDrone());
        drone.setSummoningSick(false);
        drone.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new CanopySpider());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(drone), 0, null, spider.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("Cannot be activated without two mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent drone = castDrone();
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new CanopySpider());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(drone), 0, null, spider.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetLand() {
        Permanent drone = castDrone();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(drone), 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
