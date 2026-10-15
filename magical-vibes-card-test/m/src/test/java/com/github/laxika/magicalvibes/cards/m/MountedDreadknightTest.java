package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MountedDreadknight.class)
class MountedDreadknightTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter when an opponent lost life this turn")
    void entersWithCounterAfterOpponentLifeLoss() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        castMountedDreadknight();

        assertThat(findDreadknight().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters without a +1/+1 counter when no opponent lost life this turn")
    void entersWithoutCounterWhenNoOpponentLostLife() {
        castMountedDreadknight();

        assertThat(findDreadknight().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not get a +1/+1 counter when only its controller lost life this turn")
    void ignoresControllerLifeLoss() {
        gd.lifeLostThisTurn.put(player1.getId(), 1);

        castMountedDreadknight();

        assertThat(findDreadknight().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Losing more life still grants only one counter")
    void largerLifeLossStillGrantsOneCounter() {
        gd.lifeLostThisTurn.put(player2.getId(), 7);

        castMountedDreadknight();

        assertThat(findDreadknight().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Life loss after casting is checked when the creature enters")
    void checksLifeLossAtEntryRatherThanCasting() {
        harness.castFromHand(player1, new MountedDreadknight(), "{4}{R}");
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        resolveAllTriggers();

        assertThat(findDreadknight().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Noncast entry has its counter immediately and creates no counter trigger")
    void noncastEntryHasCounterWithoutUsingStack() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);

        Permanent dreadknight = harness.enterBattlefieldAndReturn(player1, new MountedDreadknight());

        assertThat(dreadknight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessDamageThroughBlocker() {
        addCreatureReady(player1, new MountedDreadknight());
        Permanent blocker = addCreatureReady(player2, new MountedDreadknight());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 4, player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Mounted Dreadknight");
        harness.assertInGraveyard(player2, "Mounted Dreadknight");
    }

    private void castMountedDreadknight() {
        harness.castFromHand(player1, new MountedDreadknight(), "{4}{R}");
        resolveAllTriggers();
    }

    private Permanent findDreadknight() {
        return findPermanent(player1, "Mounted Dreadknight");
    }
}
