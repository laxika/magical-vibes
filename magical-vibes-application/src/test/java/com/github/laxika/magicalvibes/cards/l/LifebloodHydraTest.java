package com.github.laxika.magicalvibes.cards.l;

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

@CardUsed({LifebloodHydra.class, LightningBolt.class, GrizzlyBears.class})
class LifebloodHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters with three +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new LifebloodHydra()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent hydra = findPermanent(player1, "Lifeblood Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(hydra.getEffectivePower()).isEqualTo(3);
        assertThat(hydra.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("When it dies, its controller gains life and draws cards equal to its power")
    void deathGainsLifeAndDrawsCardsEqualToPower() {
        Permanent hydra = addCreatureReady(player1, new LifebloodHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, hydra.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        harness.assertInGraveyard(player1, "Lifeblood Hydra");
    }
}
