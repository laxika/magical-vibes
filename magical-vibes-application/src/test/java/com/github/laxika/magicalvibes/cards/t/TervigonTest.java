package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({Tervigon.class, GrizzlyBears.class})
class TervigonTest extends BaseCardTest {

    @Test
    @DisplayName("Ravenous does not draw below X=5")
    void ravenousDoesNotDrawBelowThreshold() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent tervigon = castTervigon(4);

        assertThat(tervigon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ravenous enters with X +1/+1 counters and draws at X=5")
    void ravenousEntersWithCountersAndDrawsAtThreshold() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent tervigon = castTervigon(5);

        assertThat(tervigon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Combat damage to a player creates that many Tyranid tokens")
    void createsTokensEqualToCombatDamage() {
        Permanent tervigon = castTervigon(3);
        tervigon.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(findPermanents(player1, "Tyranid")).hasSize(3);
    }

    private Permanent castTervigon(int x) {
        harness.setHand(player1, List.of(new Tervigon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, x + 1);

        harness.castCreature(player1, 0, x);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Tervigon");
    }
}
