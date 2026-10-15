package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
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

@CardUsed({NearheathStalker.class, Shock.class, GrafdiggersCage.class})
class NearheathStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Undying returns Nearheath Stalker with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new NearheathStalker());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, stalker.getId());
        harness.passBothPriorities();

        Permanent returnedStalker = findPermanent(player1, "Nearheath Stalker");
        assertThat(returnedStalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Nearheath Stalker");
    }

    @Test
    @DisplayName("Undying does not return Nearheath Stalker when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new NearheathStalker());
        stalker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, stalker.getId());

        harness.assertNotOnBattlefield(player1, "Nearheath Stalker");
        harness.assertInGraveyard(player1, "Nearheath Stalker");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Undying cannot return a creature while Grafdigger's Cage is on the battlefield")
    void cagePreventsUndyingReturn() {
        harness.addToBattlefield(player1, new GrafdiggersCage());
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new NearheathStalker());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, stalker.getId());
        harness.assertInGraveyard(player1, "Nearheath Stalker");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nearheath Stalker");
        harness.assertInGraveyard(player1, "Nearheath Stalker");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A -1/-1 counter does not prevent undying and is not retained on return")
    void undyingReturnsAfterZeroToughnessDeath() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new NearheathStalker());
        stalker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Nearheath Stalker");
        harness.assertNotOnBattlefield(player1, "Nearheath Stalker");
        harness.passBothPriorities();

        Permanent returnedStalker = findPermanent(player1, "Nearheath Stalker");
        assertThat(returnedStalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returnedStalker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Nearheath Stalker");
    }

    @Test
    @DisplayName("Undying returns a stolen creature to its owner rather than its last controller")
    void undyingReturnsToOwner() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new NearheathStalker());
        gd.playerBattlefields.get(player1.getId()).remove(stalker);
        gd.playerBattlefields.get(player2.getId()).add(stalker);
        gd.stolenCreatures.put(stalker.getId(), player1.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, stalker.getId());
        harness.assertInGraveyard(player1, "Nearheath Stalker");
        harness.passBothPriorities();

        Permanent returnedStalker = findPermanent(player1, "Nearheath Stalker");
        assertThat(returnedStalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Nearheath Stalker");
        harness.assertNotInGraveyard(player1, "Nearheath Stalker");
        harness.assertNotInGraveyard(player2, "Nearheath Stalker");
    }
}
