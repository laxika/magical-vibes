package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BrindleBoar;
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

@CardUsed({VoraciousWurm.class, BrindleBoar.class, Shock.class})
class VoraciousWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with no counters when controller has gained no life this turn")
    void entersWithNoCountersWhenNoLifeGained() {
        harness.castFromHand(player1, new VoraciousWurm(), "{1}{G}");
        harness.passBothPriorities();

        Permanent wurm = findPermanent(player1, "Voracious Wurm");
        assertThat(wurm).isNotNull();
        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Enters with counters equal to life gained this turn by controller")
    void entersWithCountersEqualToLifeGained() {
        gd.lifeGainedThisTurn.put(player1.getId(), 5);

        harness.castFromHand(player1, new VoraciousWurm(), "{1}{G}");
        harness.passBothPriorities();

        Permanent wurm = findPermanent(player1, "Voracious Wurm");
        assertThat(wurm).isNotNull();
        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not count opponent's life gained this turn")
    void ignoresOpponentLifeGained() {
        gd.lifeGainedThisTurn.put(player2.getId(), 7);

        harness.castFromHand(player1, new VoraciousWurm(), "{1}{G}");
        harness.passBothPriorities();

        Permanent wurm = findPermanent(player1, "Voracious Wurm");
        assertThat(wurm).isNotNull();
        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Counts multiple life gains without subtracting subsequent damage")
    void countsTotalLifeGainedRatherThanNetLifeChange() {
        addCreatureReady(player1, new BrindleBoar());
        addCreatureReady(player1, new BrindleBoar());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.castFromHand(player1, new VoraciousWurm(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Voracious Wurm")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Includes life gained while the creature spell is on the stack")
    void countsLifeGainedInResponse() {
        addCreatureReady(player1, new BrindleBoar());
        harness.castFromHand(player1, new VoraciousWurm(), "{1}{G}");
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Voracious Wurm")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Life gained after entry does not add counters")
    void doesNotGrowAfterEntering() {
        addCreatureReady(player1, new BrindleBoar());
        harness.castFromHand(player1, new VoraciousWurm(), "{1}{G}");
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLifeGainedThisTurn(player1.getId())).isEqualTo(4);
        assertThat(findPermanent(player1, "Voracious Wurm")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
