package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheSwarmlord.class, Forest.class, LlanowarElves.class, Murder.class, Shock.class})
class TheSwarmlordTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters for each prior commander cast")
    void entersWithCountersForCommanderCasts() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        gd.recordCommanderCastFromCommandZone(player1.getId());

        Permanent swarmlord = harness.enterBattlefieldAndReturn(player1, new TheSwarmlord());

        assertThat(swarmlord.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Draws when a controlled creature with any counter dies")
    void drawsWhenCounterBearerDies() {
        addSwarmlord();
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elves.setCounterCount(CounterType.CHARGE, 1);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, elves.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Does not draw when a controlled creature dies without a counter")
    void doesNotDrawWhenDyingCreatureHasNoCounter() {
        addSwarmlord();
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, elves.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Draws when The Swarmlord dies with a counter")
    void drawsWhenTheSwarmlordDiesWithCounter() {
        Permanent swarmlord = addSwarmlord();
        swarmlord.setCounterCount(CounterType.CHARGE, 1);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, swarmlord.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    void entersWithoutCountersWhenOnlyOpponentHasCastCommander() {
        gd.recordCommanderCastFromCommandZone(player2.getId());

        Permanent swarmlord = addSwarmlord();

        assertThat(swarmlord.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castingFromHandDoesNotIncreaseCommanderCastCount() {
        harness.castFromHand(player1, new TheSwarmlord(), "{3}{G}{U}{R}");
        harness.passBothPriorities();

        Permanent swarmlord = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(swarmlord.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotDrawForOpponentCreatureWithCounter() {
        addSwarmlord();
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        elves.setCounterCount(CounterType.CHARGE, 1);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, elves.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    void doesNotDrawWhenSwarmlordDiesWithoutCounters() {
        Permanent swarmlord = addSwarmlord();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, swarmlord.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void drawsForPlusOneCounterAndOnlyOnceForMultipleCounters() {
        addSwarmlord();
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elves.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        elves.setCounterCount(CounterType.CHARGE, 3);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, elves.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    void drawsOncePerCounterBearerWhenSwarmlordAndAllyDieSimultaneously() {
        Permanent swarmlord = addSwarmlord();
        swarmlord.setCounterCount(CounterType.CHARGE, 3);
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elves.setCounterCount(CounterType.CHARGE, 2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());
        swarmlord.setMarkedDamage(5);
        elves.setMarkedDamage(1);

        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "The Swarmlord");
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    private Permanent addSwarmlord() {
        return harness.enterBattlefieldAndReturn(player1, new TheSwarmlord());
    }
}
