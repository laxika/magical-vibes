package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AfiyaGroveTest extends BaseCardTest {

    private Permanent castGrove(Player player) {
        harness.setHand(player, List.of(new AfiyaGrove()));
        harness.addMana(player, ManaColor.GREEN, 3);
        harness.castEnchantment(player, 0);
        harness.passBothPriorities();
        return findPermanent(player, "Afiya Grove");
    }

    @Test
    @DisplayName("Enters with three +1/+1 counters")
    void entersWithThreeCounters() {
        Permanent grove = castGrove(player1);

        assertThat(grove.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Upkeep trigger moves a +1/+1 counter onto the chosen creature")
    void upkeepTriggerMovesCounter() {
        Permanent grove = castGrove(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(grove.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The counter can be moved onto an opponent's creature")
    void canMoveCounterOntoOpponentCreature() {
        Permanent grove = castGrove(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(grove.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Trigger does nothing with no creature on the battlefield")
    void noCreatureMeansNoTrigger() {
        Permanent grove = castGrove(player1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(grove.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrificed once its last +1/+1 counter is moved off")
    void sacrificedWhenLastCounterLeaves() {
        Permanent grove = castGrove(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        grove.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Afiya Grove")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> "Afiya Grove".equals(c.getName()));
    }
}
