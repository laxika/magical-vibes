package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PterafractylTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X +1/+1 counters and its controller gains 2 life")
    void entersWithCountersAndGainsLife() {
        harness.setHand(player1, List.of(new Pterafractyl()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent pterafractyl = findPermanent(player1, "Pterafractyl");
        assertThat(pterafractyl.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(pterafractyl.getEffectivePower()).isEqualTo(4);
        assertThat(pterafractyl.getEffectiveToughness()).isEqualTo(3);
        harness.assertLife(player1, 22);
    }
}
