package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrizzlyGhoul.class, GrizzlyBears.class, Shock.class})
class GrizzlyGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Enters without counters when no creature died this turn")
    void entersWithNoCountersWhenNoDeaths() {
        castGhoul();

        assertThat(findGhoul().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Enters with a counter for each creature that died this turn, any player")
    void entersWithCountersForAllDeaths() {
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);
        gd.creatureDeathCountThisTurn.put(player2.getId(), 3);

        castGhoul();

        assertThat(findGhoul().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts a creature actually killed earlier in the turn")
    void entersWithCounterAfterActualDeath() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bearsId);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        castGhoul();

        assertThat(findGhoul().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts creatures from both players that actually died")
    void countsActualDeathsFromBothPlayers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID ownBearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingBearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, ownBearsId);
        harness.castAndResolveInstant(player1, 0, opposingBearsId);

        castGhoul();

        assertThat(findGhoul().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts deaths after casting but before entering")
    void countsDeathWhileGhoulIsOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castFromHand(player1, new GrizzlyGhoul(), "{2}{B}{G}");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bearsId);
        harness.passBothPriorities();

        assertThat(findGhoul().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Later deaths do not add counters to a Ghoul already on the battlefield")
    void doesNotGainCountersForLaterDeaths() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castGhoul();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bearsId);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        assertThat(findGhoul().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The entry replacement also applies when entering without being cast")
    void entersWithCountersWithoutBeingCast() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bearsId);

        Permanent ghoul = harness.enterBattlefieldAndReturn(player1, new GrizzlyGhoul());

        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void castGhoul() {
        harness.castFromHand(player1, new GrizzlyGhoul(), "{2}{B}{G}");
        harness.passBothPriorities();
    }

    private Permanent findGhoul() {
        return findPermanent(player1, "Grizzly Ghoul");
    }
}
