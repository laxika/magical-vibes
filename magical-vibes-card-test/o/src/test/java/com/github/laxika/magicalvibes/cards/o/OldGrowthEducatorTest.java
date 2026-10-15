package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.w.WitherbloomCharm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OldGrowthEducator.class, WitherbloomCharm.class})
class OldGrowthEducatorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters without counters if no life was gained this turn")
    void entersWithoutCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new OldGrowthEducator(), "{2}{B}{G}");
        harness.passBothPriorities(); // resolve creature spell (enters + ETB trigger)
        harness.passBothPriorities(); // resolve the ETB trigger (does nothing — no life gained)

        GameData gd = harness.getGameData();
        Permanent educator = findPermanent(player1, "Old-Growth Educator");
        assertThat(educator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Enters with two +1/+1 counters when you have gained life this turn")
    void entersWithCountersWhenLifeGained() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        harness.castFromHand(player1, new OldGrowthEducator(), "{2}{B}{G}");
        harness.passBothPriorities(); // resolve creature spell (enters + ETB trigger)
        harness.passBothPriorities(); // resolve ETB effect (put counters)

        Permanent educator = findPermanent(player1, "Old-Growth Educator");
        assertThat(educator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(educator.getEffectivePower()).isEqualTo(6);
        assertThat(educator.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Infusion triggers without prior life gain and checks life gain on resolution")
    void gainsCountersWhenLifeIsGainedInResponseToInfusion() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new OldGrowthEducator(), "{2}{B}{G}");
        harness.passBothPriorities();

        Permanent educator = findPermanent(player1, "Old-Growth Educator");
        assertThat(educator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new WitherbloomCharm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();
        assertThat(educator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(educator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent gaining life does not satisfy your Infusion condition")
    void opponentLifeGainDoesNotAddCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new WitherbloomCharm()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castModalInstant(player2, 0, 1, List.of());
        harness.passBothPriorities();

        harness.castFromHand(player1, new OldGrowthEducator(), "{2}{B}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent educator = findPermanent(player1, "Old-Growth Educator");
        assertThat(educator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
