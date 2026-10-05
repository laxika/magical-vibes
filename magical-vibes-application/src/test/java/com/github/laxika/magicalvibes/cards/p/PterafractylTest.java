package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Pterafractyl.class})
class PterafractylTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X +1/+1 counters and its controller gains 2 life")
    void entersWithCountersAndGainsLife() {
        harness.setHand(player1, List.of(new Pterafractyl()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        resolveAllTriggers();

        Permanent pterafractyl = findPermanent(player1, "Pterafractyl");
        assertThat(pterafractyl.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(pterafractyl.getEffectivePower()).isEqualTo(4);
        assertThat(pterafractyl.getEffectiveToughness()).isEqualTo(3);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Counters are present on entry before the life-gain trigger resolves")
    void countersArePresentBeforeLifeGain() {
        harness.setHand(player1, List.of(new Pterafractyl()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent pterafractyl = findPermanent(player1, "Pterafractyl");
        assertThat(pterafractyl.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(pterafractyl.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Casting with X zero still gains life after the creature dies")
    void zeroCountersStillGainsLife() {
        harness.setHand(player1, List.of(new Pterafractyl()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pterafractyl");
        harness.assertInGraveyard(player1, "Pterafractyl");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }
}
