package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KitchenFinks.class, DoomBlade.class})
class KitchenFinksTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains 2 life")
    void etbGainsTwoLife() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new KitchenFinks()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Persist returns Kitchen Finks with a -1/-1 counter when it dies with no -1/-1 counters")
    void persistReturnsWithMinusCounter() {
        harness.addToBattlefield(player1, new KitchenFinks());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Kitchen Finks"));
        resolveAllTriggers();

        Permanent finks = findPermanent(player1, "Kitchen Finks");
        assertThat(finks).isNotNull();
        assertThat(finks.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(finks.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Persist does not return Kitchen Finks when it died with a -1/-1 counter")
    void persistDoesNotReturnWithExistingMinusCounter() {
        Permanent finks = harness.addToBattlefieldAndReturn(player1, new KitchenFinks());
        finks.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, finks.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Kitchen Finks");
        harness.assertInGraveyard(player1, "Kitchen Finks");
    }

    @Test
    @DisplayName("Persist gains life again, but a second death does not return it")
    void persistGainsLifeAndOnlyReturnsOnce() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new KitchenFinks());
        harness.setHand(player1, List.of(new DoomBlade(), new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Kitchen Finks"));
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        Permanent returned = findPermanent(player1, "Kitchen Finks");
        assertThat(returned).isNotNull();
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        harness.castInstant(player1, 0, returned.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Kitchen Finks");
        harness.assertInGraveyard(player1, "Kitchen Finks");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("The entering controller gains life, including when the opponent casts it")
    void opponentGainsLifeFromEntering() {
        harness.forceActivePlayer(player2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);
        harness.setHand(player2, List.of(new KitchenFinks()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 12);
    }
}
