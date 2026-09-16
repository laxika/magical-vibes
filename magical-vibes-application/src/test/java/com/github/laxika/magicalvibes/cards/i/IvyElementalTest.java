package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IvyElemental.class})
class IvyElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters with 3 +1/+1 counters")
    void entersWithThreeCounters() {
        harness.setHand(player1, List.of(new IvyElemental()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent ivyElemental = findPermanent(player1, "Ivy Elemental");
        assertThat(ivyElemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting with X=0 puts a 0/0 Ivy Elemental into the graveyard")
    void entersWithZeroCountersAndDies() {
        harness.setHand(player1, List.of(new IvyElemental()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ivy Elemental");
        harness.assertInGraveyard(player1, "Ivy Elemental");
    }
}
