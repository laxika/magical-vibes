package com.github.laxika.magicalvibes.cards.y;

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

@CardUsed({YoungWolf.class, Shock.class, GrafdiggersCage.class})
class YoungWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Undying returns Young Wolf with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new YoungWolf());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, wolf.getId());
        harness.passBothPriorities();

        Permanent returnedWolf = findPermanent(player1, "Young Wolf");
        assertThat(returnedWolf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Young Wolf");
    }

    @Test
    @DisplayName("Undying does not return Young Wolf when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new YoungWolf());
        wolf.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, wolf.getId());

        harness.assertNotOnBattlefield(player1, "Young Wolf");
        harness.assertInGraveyard(player1, "Young Wolf");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Undying returns a stolen Young Wolf under its owner's control")
    void undyingReturnsToOwner() {
        YoungWolf card = new YoungWolf();
        card.setOwnerId(player1.getId());
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, card);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, wolf.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Young Wolf");
        Permanent returnedWolf = findPermanent(player1, "Young Wolf");
        assertThat(returnedWolf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Young Wolf");
        harness.assertNotInGraveyard(player2, "Young Wolf");
    }

    @Test
    @DisplayName("Grafdigger's Cage prevents the undying return")
    void cagePreventsUndyingReturn() {
        harness.addToBattlefield(player1, new GrafdiggersCage());
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new YoungWolf());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, wolf.getId());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Young Wolf");
        harness.assertInGraveyard(player1, "Young Wolf");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Young Wolf returned by undying stays dead on its next death")
    void returnedWolfDoesNotReturnAgain() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new YoungWolf());
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, wolf.getId());
        resolveAllTriggers();
        Permanent returnedWolf = findPermanent(player1, "Young Wolf");

        harness.castAndResolveInstant(player2, 0, returnedWolf.getId());

        harness.assertNotOnBattlefield(player1, "Young Wolf");
        harness.assertInGraveyard(player1, "Young Wolf");
        assertThat(gd.stack).isEmpty();
    }
}
