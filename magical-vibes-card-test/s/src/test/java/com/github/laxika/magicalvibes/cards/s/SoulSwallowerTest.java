package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulSwallower.class, GrizzlyBears.class, Forest.class, Shock.class, Millstone.class})
class SoulSwallowerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not get counters without delirium")
    void doesNotGetCountersWithoutDelirium() {
        Permanent soulSwallower = harness.addToBattlefieldAndReturn(player1, new SoulSwallower());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(soulSwallower.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Gets three +1/+1 counters with delirium")
    void getsThreeCountersWithDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        Permanent soulSwallower = harness.addToBattlefieldAndReturn(player1, new SoulSwallower());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(soulSwallower.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Four cards of only three types do not enable delirium")
    void fourCardsOfThreeTypesDoNotEnableDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new Forest(), new Shock()));
        Permanent soulSwallower = harness.addToBattlefieldAndReturn(player1, new SoulSwallower());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();
        assertThat(soulSwallower.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Delirium gained after upkeep begins does not create a trigger")
    void gainingDeliriumAfterUpkeepDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        Permanent soulSwallower = harness.addToBattlefieldAndReturn(player1, new SoulSwallower());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        gd.playerGraveyards.get(player1.getId()).add(new Millstone());
        harness.passBothPriorities();
        assertThat(soulSwallower.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Losing delirium before resolution prevents counters")
    void losingDeliriumBeforeResolutionPreventsCounters() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        Permanent soulSwallower = harness.addToBattlefieldAndReturn(player1, new SoulSwallower());

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        gd.playerGraveyards.get(player1.getId()).removeIf(card -> card instanceof Millstone);
        harness.passBothPriorities();
        assertThat(soulSwallower.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable delirium")
    void opponentsGraveyardDoesNotEnableDelirium() {
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        Permanent soulSwallower = harness.addToBattlefieldAndReturn(player1, new SoulSwallower());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();
        assertThat(soulSwallower.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        Permanent soulSwallower = harness.addToBattlefieldAndReturn(player1, new SoulSwallower());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();
        assertThat(soulSwallower.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
