package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodsporeThrinax.class, GrizzlyBears.class})
class BloodsporeThrinaxTest extends BaseCardTest {

    @Test
    @DisplayName("Devouring a creature gives Bloodspore Thrinax a counter and boosts the next creature")
    void devourCountersBoostNextCreature() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBloodsporeThrinax();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder.getId()));

        Permanent thrinax = findPermanent(player1, "Bloodspore Thrinax");
        assertThat(thrinax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter grant uses Bloodspore Thrinax's current counter count")
    void counterGrantUsesCurrentCounterCount() {
        Permanent thrinax = harness.addToBattlefieldAndReturn(player1, new BloodsporeThrinax());
        thrinax.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        Permanent ownCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Devouring no creatures leaves Bloodspore Thrinax and later creatures without counters")
    void devourNoneAddsNoCounters() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castBloodsporeThrinax();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        Permanent thrinax = findPermanent(player1, "Bloodspore Thrinax");
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(thrinax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castBloodsporeThrinax() {
        harness.setHand(player1, List.of(new BloodsporeThrinax()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
    }
}
