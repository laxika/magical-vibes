package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AssaultSuit;
import com.github.laxika.magicalvibes.cards.l.LivingTerrain;
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

@CardUsed({RemoteFarm.class, LivingTerrain.class, AssaultSuit.class})
class RemoteFarmTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with two depletion counters")
    void entersTappedWithTwoDepletionCounters() {
        harness.setHand(player1, List.of(new RemoteFarm()));
        harness.playLand(player1, 0);

        Permanent farm = findPermanent(player1, "Remote Farm");
        assertThat(farm.isTapped()).isTrue();
        assertThat(farm.getCounterCount(CounterType.DEPLETION)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing a depletion counter adds two white mana and keeps the land")
    void removesCounterAndAddsTwoWhiteMana() {
        Permanent farm = addFarm(2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(whiteMana()).isEqualTo(2);
        assertThat(farm.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Remote Farm");
    }

    @Test
    @DisplayName("Removing the last depletion counter adds mana and sacrifices the land")
    void removesLastCounterAndSacrifices() {
        Permanent farm = addFarm(1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(whiteMana()).isEqualTo(2);
        assertThat(farm.getCounterCount(CounterType.DEPLETION)).isZero();
        harness.assertNotOnBattlefield(player1, "Remote Farm");
        harness.assertInGraveyard(player1, "Remote Farm");
    }

    @Test
    @DisplayName("Cannot activate without a depletion counter")
    void cannotActivateWithoutDepletionCounter() {
        addFarm(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while Remote Farm is tapped")
    void cannotActivateWhenTapped() {
        addFarm(2);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("An untapped newly entered land can produce mana immediately")
    void newlyEnteredLandCanProduceManaWhenUntapped() {
        harness.setHand(player1, List.of(new RemoteFarm()));
        harness.playLand(player1, 0);
        Permanent farm = findPermanent(player1, "Remote Farm");
        farm.untap();

        harness.activateAbility(player1, 0, null, null);

        assertThat(whiteMana()).isEqualTo(2);
        assertThat(farm.isTapped()).isTrue();
        assertThat(farm.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Remote Farm");
    }

    @Test
    @DisplayName("Other counters do not prevent sacrifice after the last depletion counter")
    void otherCountersDoNotPreventSacrifice() {
        Permanent farm = addFarm(1);
        farm.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(whiteMana()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Remote Farm");
        harness.assertInGraveyard(player1, "Remote Farm");
    }

    @Test
    @DisplayName("Other counters cannot pay the depletion counter cost")
    void otherCountersCannotPayActivationCost() {
        Permanent farm = addFarm(0);
        farm.setCounterCount(CounterType.CHARGE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(whiteMana()).isZero();
        assertThat(farm.isTapped()).isFalse();
        assertThat(farm.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Remote Farm");
    }

    @Test
    @DisplayName("A land that cannot be sacrificed still produces mana using its last counter")
    void sacrificeProtectionKeepsLandAfterLastCounter() {
        Permanent farm = addFarm(1);
        Permanent terrain = harness.addToBattlefieldAndReturn(player1, new LivingTerrain());
        terrain.setAttachedTo(farm.getId());
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new AssaultSuit());
        suit.setAttachedTo(farm.getId());

        assertThat(gqs.isCreature(gd, farm)).isTrue();
        assertThat(gqs.cantBeSacrificed(gd, farm)).isTrue();

        harness.activateAbility(player1, 0, null, null);

        assertThat(whiteMana()).isEqualTo(2);
        assertThat(farm.getCounterCount(CounterType.DEPLETION)).isZero();
        assertThat(farm.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Remote Farm");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private Permanent addFarm(int counters) {
        Permanent farm = harness.addToBattlefieldAndReturn(player1, new RemoteFarm());
        farm.setSummoningSick(false);
        farm.setCounterCount(CounterType.DEPLETION, counters);
        return farm;
    }

    private int whiteMana() {
        return gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE);
    }
}
