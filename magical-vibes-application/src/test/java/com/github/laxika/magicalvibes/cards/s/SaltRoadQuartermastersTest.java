package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SaltRoadQuartermasters.class, GrizzlyBears.class, Forest.class})
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
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addReadyQuartermasters(Player player) {
        Permanent quartermasters = new Permanent(new SaltRoadQuartermasters());
        quartermasters.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        quartermasters.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(quartermasters);
        return quartermasters;
    }
}
