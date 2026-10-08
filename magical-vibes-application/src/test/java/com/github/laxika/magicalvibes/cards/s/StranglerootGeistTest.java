package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.t.TragicSlip;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StranglerootGeist.class, Shock.class, TragicSlip.class, GrafdiggersCage.class})
class StranglerootGeistTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack the turn it enters the battlefield due to haste")
    void canAttackWithSummoningSicknessDueToHaste() {
        harness.castFromHand(player1, new StranglerootGeist(), "{G}{G}");
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));

        Permanent geist = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(geist.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Undying returns Strangleroot Geist with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new StranglerootGeist());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, geist.getId());
        harness.passBothPriorities();

        Permanent returnedGeist = findPermanent(player1, "Strangleroot Geist");
        assertThat(returnedGeist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Strangleroot Geist");
    }

    @Test
    @DisplayName("Undying does not return Strangleroot Geist when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new StranglerootGeist());
        geist.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, geist.getId());

        harness.assertNotOnBattlefield(player1, "Strangleroot Geist");
        harness.assertInGraveyard(player1, "Strangleroot Geist");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Undying returns a creature that dies from zero toughness as a new untapped permanent")
    void undyingReturnsAfterToughnessReduction() {
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new StranglerootGeist());
        geist.tap();
        harness.setHand(player2, List.of(new TragicSlip()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player2, 0, geist.getId());
        resolveAllTriggers();

        Permanent returnedGeist = findPermanent(player1, "Strangleroot Geist");
        assertThat(returnedGeist.getId()).isNotEqualTo(geist.getId());
        assertThat(returnedGeist.isTapped()).isFalse();
        assertThat(returnedGeist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Strangleroot Geist");
    }

    @Test
    @DisplayName("Grafdigger's Cage prevents the undying return")
    void cagePreventsUndyingReturn() {
        harness.addToBattlefield(player2, new GrafdiggersCage());
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new StranglerootGeist());
        harness.setHand(player2, List.of(new TragicSlip()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player2, 0, geist.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Strangleroot Geist");
        harness.assertInGraveyard(player1, "Strangleroot Geist");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Undying returns a creature controlled by the opponent to its owner")
    void undyingReturnsToOwner() {
        StranglerootGeist card = new StranglerootGeist();
        card.setOwnerId(player1.getId());
        Permanent geist = harness.addToBattlefieldAndReturn(player2, card);
        harness.setHand(player1, List.of(new TragicSlip()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, geist.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Strangleroot Geist");
        Permanent returnedGeist = findPermanent(player1, "Strangleroot Geist");
        assertThat(returnedGeist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Strangleroot Geist");
        harness.assertNotInGraveyard(player2, "Strangleroot Geist");
    }
}
