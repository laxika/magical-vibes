package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({VoraciousVermin.class, GrizzlyBears.class, Shock.class})
class VoraciousVerminTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Rat token that can't block when it enters")
    void createsNonblockingRatOnEnter() {
        castAndResolve();

        Permanent rat = findPermanents(player1, "Rat").getFirst();
        assertThat(bls.canBlock(gd, rat)).isFalse();
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when another creature you control dies")
    void getsCounterWhenAllyCreatureDies() {
        castAndResolve();
        Permanent vermin = findPermanent(player1, "Voracious Vermin");
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(vermin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a counter when an opponent's creature dies")
    void ignoresOpponentCreatureDeaths() {
        castAndResolve();
        Permanent vermin = findPermanent(player1, "Voracious Vermin");
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(vermin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Gets a counter when its Rat token dies")
    void getsCounterWhenTokenDies() {
        castAndResolve();
        Permanent vermin = findPermanent(player1, "Voracious Vermin");
        Permanent rat = findPermanent(player1, "Rat");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, rat.getId());

        assertThat(findPermanents(player1, "Rat")).isEmpty();
        assertThat(vermin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(vermin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers for each allied creature death in the same turn")
    void triggersForEachDeathInSameTurn() {
        castAndResolve();
        Permanent vermin = findPermanent(player1, "Voracious Vermin");
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent rat = findPermanent(player1, "Rat");
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, bear.getId());
        resolveAllTriggers();
        harness.castAndResolveInstant(player1, 0, rat.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Rat")).isEmpty();
        assertThat(vermin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for its own death")
    void doesNotTriggerForOwnDeath() {
        castAndResolve();
        Permanent vermin = findPermanent(player1, "Voracious Vermin");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, vermin.getId());

        harness.assertInGraveyard(player1, "Voracious Vermin");
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Rat")).hasSize(1);
    }

    @Test
    @DisplayName("Creates its Rat even if it dies before the enter trigger resolves")
    void enterTriggerResolvesAfterSourceDies() {
        harness.setHand(player1, List.of(new VoraciousVermin(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent vermin = findPermanent(player1, "Voracious Vermin");
        assertThat(findPermanents(player1, "Rat")).isEmpty();

        harness.castAndResolveInstant(player1, 0, vermin.getId());
        harness.assertInGraveyard(player1, "Voracious Vermin");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Rat")).hasSize(1);
        assertThat(bls.canBlock(gd, findPermanent(player1, "Rat"))).isFalse();
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new VoraciousVermin()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
