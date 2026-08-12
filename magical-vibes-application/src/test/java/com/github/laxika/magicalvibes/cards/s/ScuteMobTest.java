package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScuteMobTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your upkeep, five lands put four +1/+1 counters on Scute Mob")
    void putsCountersWithFiveLands() {
        Permanent mob = addMob();
        addForests(player1, 5);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(mob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Fewer than five lands do not put counters on Scute Mob")
    void doesNotPutCountersWithFewerThanFiveLands() {
        Permanent mob = addMob();
        addForests(player1, 4);
        addForests(player2, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(mob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The upkeep trigger does nothing if the controller has fewer than five lands at resolution")
    void doesNothingIfLandCountFallsBeforeResolution() {
        Permanent mob = addMob();
        addForests(player1, 5);

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Forest"));
        harness.passBothPriorities();

        assertThat(mob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addMob() {
        return harness.addToBattlefieldAndReturn(player1, new ScuteMob());
    }

    private void addForests(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }
}
