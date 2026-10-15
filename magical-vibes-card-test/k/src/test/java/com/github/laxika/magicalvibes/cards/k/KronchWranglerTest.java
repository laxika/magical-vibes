package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KronchWrangler.class, AirElemental.class, GrizzlyBears.class,
        GloriousAnthem.class, GiantGrowth.class, Boomerang.class})
class KronchWranglerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when a power-4 creature you control enters")
    void putsCounterOnSelfForPowerFourAlly() {
        Permanent kronch = addCreatureReady(player1, new KronchWrangler());

        harness.castFromHand(player1, new AirElemental(), "{3}{U}{U}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(kronch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for a creature with power less than 4")
    void doesNotTriggerForSmallAlly() {
        Permanent kronch = addCreatureReady(player1, new KronchWrangler());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(kronch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger for a power-4 creature controlled by an opponent")
    void doesNotTriggerForOpponentCreature() {
        Permanent kronch = addCreatureReady(player1, new KronchWrangler());
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new AirElemental(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(kronch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Triggers for its own entry when static boosts give it power 4")
    void triggersForOwnEntryWithPowerFour() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new GloriousAnthem());

        harness.castFromHand(player1, new KronchWrangler(), "{1}{G}");
        harness.passBothPriorities();
        Permanent kronch = findPermanent(player1, "Kronch Wrangler");
        resolveAllTriggers();

        assertThat(kronch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts static boosts when checking the entering ally's power")
    void triggersForStaticallyBoostedAlly() {
        Permanent kronch = addCreatureReady(player1, new KronchWrangler());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new GloriousAnthem());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(kronch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Still adds a counter if the entering creature's power falls below 4")
    void doesNotRecheckPowerOnResolution() {
        Permanent kronch = addCreatureReady(player1, new KronchWrangler());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, anthem.getId());
        resolveAllTriggers();

        assertThat(kronch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Raising a creature's power after entry does not trigger the ability")
    void doesNotTriggerForPowerRaisedAfterEntry() {
        Permanent kronch = addCreatureReady(player1, new KronchWrangler());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(kronch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An old trigger does not put a counter on a Wrangler that leaves and returns")
    void oldTriggerDoesNotFollowReturnedSource() {
        Permanent original = addCreatureReady(player1, new KronchWrangler());
        harness.castFromHand(player1, new AirElemental(), "{3}{U}{U}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, original.getId());
        harness.assertInHand(player1, "Kronch Wrangler");
        Permanent returned = harness.enterBattlefieldAndReturn(player1,
                gd.playerHands.get(player1.getId()).remove(0));
        resolveAllTriggers();

        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
