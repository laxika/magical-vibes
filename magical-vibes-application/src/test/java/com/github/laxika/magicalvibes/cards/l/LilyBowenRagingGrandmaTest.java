package com.github.laxika.magicalvibes.cards.l;

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

@CardUsed({LilyBowenRagingGrandma.class})
class LilyBowenRagingGrandmaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters")
    void entersWithTwoCounters() {
        harness.setHand(player1, List.of(new LilyBowenRagingGrandma()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent lily = findPermanent(player1, "Lily Bowen, Raging Grandma");
        assertThat(lily.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Doubles its counters when its power is 16 or less")
    void doublesCountersAtPowerSixteenOrLess() {
        Permanent lily = addLilyReady(player1, 16);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(lily.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(32);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Keeps one counter and gains life for the counters removed above power 16")
    void keepsOneCounterAndGainsLifeAbovePowerSixteen() {
        Permanent lily = addLilyReady(player1, 20);
        int startingLife = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(lily.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife + 19);
    }

    private Permanent addLilyReady(Player player, int counters) {
        Permanent lily = addCreatureReady(player, new LilyBowenRagingGrandma());
        lily.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return lily;
    }
}
