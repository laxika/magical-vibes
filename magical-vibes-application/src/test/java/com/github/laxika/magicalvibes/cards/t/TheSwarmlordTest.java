package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({TheSwarmlord.class, Forest.class, GrizzlyBears.class, LlanowarElves.class, Murder.class, Shock.class})
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
        harness.castInstant(player1, 0, elves.getId());
        harness.passBothPriorities();
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
        harness.castInstant(player1, 0, elves.getId());
        harness.passBothPriorities();

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
        harness.castSorcery(player1, 0, swarmlord.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    private Permanent addSwarmlord() {
        return harness.enterBattlefieldAndReturn(player1, new TheSwarmlord());
    }
}
