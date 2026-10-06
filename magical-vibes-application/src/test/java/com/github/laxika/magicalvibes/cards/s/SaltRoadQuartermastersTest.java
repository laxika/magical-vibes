package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaltRoadQuartermasters.class, ColossodonYearling.class, Forest.class})
class SaltRoadQuartermastersTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with two +1/+1 counters")
    void entersWithTwoCounters() {
        harness.setHand(player1, List.of(new SaltRoadQuartermasters()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent quartermasters = findPermanent(player1, "Salt Road Quartermasters");
        assertThat(quartermasters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes a +1/+1 counter and puts one on target creature")
    void movesCounterToTargetCreature() {
        Permanent quartermasters = addReadyQuartermasters(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(quartermasters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent quartermasters = addReadyQuartermasters(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        assertThat(quartermasters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot activate without a +1/+1 counter")
    void cannotActivateWithoutCounter() {
        Permanent quartermasters = addReadyQuartermasters(player1);
        quartermasters.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Can target itself while summoning sick and tapped, paying the counter before resolution")
    void canTargetItselfWhileSummoningSickAndTapped() {
        Permanent quartermasters = harness.enterBattlefieldAndReturn(player1, new SaltRoadQuartermasters());
        quartermasters.setSummoningSick(true);
        quartermasters.setTapped(true);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, quartermasters.getId());

        assertThat(quartermasters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(quartermasters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counter cost remains paid when the target leaves before resolution")
    void targetLeavingDoesNotRefundCounter() {
        Permanent quartermasters = addReadyQuartermasters(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(quartermasters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(quartermasters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability resolves even if its source leaves the battlefield")
    void sourceLeavingDoesNotPreventCounterPlacement() {
        Permanent quartermasters = addReadyQuartermasters(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(quartermasters);
        gd.playerGraveyards.get(player1.getId()).add(quartermasters.getCard());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can spend the last counter on another creature without sacrificing the source")
    void canSpendLastCounter() {
        Permanent quartermasters = addReadyQuartermasters(player1);
        quartermasters.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(quartermasters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(quartermasters);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot pay the green mana requirement with colorless mana")
    void cannotActivateWithoutGreenMana() {
        Permanent quartermasters = addReadyQuartermasters(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(quartermasters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyQuartermasters(Player player) {
        Permanent quartermasters = addCreatureReady(player, new SaltRoadQuartermasters());
        quartermasters.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        return quartermasters;
    }
}
