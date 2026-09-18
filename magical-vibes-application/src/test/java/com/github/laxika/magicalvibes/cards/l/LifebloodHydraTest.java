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

@CardUsed({LifebloodHydra.class, GrizzlyBears.class})
class LifebloodHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X +1/+1 counters")
    void entersWithXPlusOnePlusOneCounters() {
        castHydra(4);

        Permanent hydra = findPermanent(player1, "Lifeblood Hydra");

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("When it dies, you gain life and draw cards equal to its power")
    void deathGainsLifeAndDrawsEqualToPower() {
        harness.setLife(player1, 10);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        castHydra(3);

        Permanent hydra = findPermanent(player1, "Lifeblood Hydra");
        int handBefore = gd.playerHands.get(player1.getId()).size();
        hydra.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    private void castHydra(int xValue) {
        harness.setHand(player1, List.of(new LifebloodHydra()));
        harness.addMana(player1, ManaColor.GREEN, xValue + 3);
        gs.playCard(gd, player1, 0, xValue, null, null);
        harness.passBothPriorities();
    }
}
