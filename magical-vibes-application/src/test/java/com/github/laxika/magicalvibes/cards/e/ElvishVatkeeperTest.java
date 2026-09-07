package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ElvishVatkeeper.class)
class ElvishVatkeeperTest extends BaseCardTest {

    @Test
    void incubatesAndTransformsAnIncubatorWhileDoublingItsCounters() {
        harness.setHand(player1, List.of(new ElvishVatkeeper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        Permanent vatkeeper = findPermanent(player1, "Elvish Vatkeeper");
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(vatkeeper), null, incubator.getId());
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }
}
