package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightMarketAeronaut.class, Ornithopter.class})
class NightMarketAeronautTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter after your permanent leaves the battlefield")
    void entersWithCounterAfterYourPermanentLeaves() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        harness.setHand(player1, List.of(new NightMarketAeronaut()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Night Market Aeronaut").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters without a counter when no permanent left the battlefield")
    void entersWithoutCounterWithoutRevolt() {
        harness.setHand(player1, List.of(new NightMarketAeronaut()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Night Market Aeronaut").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's permanent leaving the battlefield does not satisfy revolt")
    void opponentPermanentLeavingDoesNotSatisfyRevolt() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        harness.setHand(player1, List.of(new NightMarketAeronaut()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Night Market Aeronaut").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Revolt checks departures at entry rather than when the spell is cast")
    void departureWhileSpellOnStackSatisfiesRevolt() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new NightMarketAeronaut()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Night Market Aeronaut").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple departures still give exactly one counter")
    void multipleDeparturesGiveOnlyOneCounter() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToHand(gd, first);
            harness.getPermanentRemovalService().removePermanentToHand(gd, second);
        });
        harness.setHand(player1, List.of(new NightMarketAeronaut()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Night Market Aeronaut").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A departure on a previous turn does not satisfy revolt")
    void previousTurnDepartureDoesNotSatisfyRevolt() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NightMarketAeronaut()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Night Market Aeronaut").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
