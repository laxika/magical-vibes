package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrakmawSkyclaveRavager.class, Assassinate.class, GrizzlyBears.class})
class GrakmawSkyclaveRavagerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three +1/+1 counters")
    void entersWithThreeCounters() {
        harness.setHand(player1, List.of(new GrakmawSkyclaveRavager()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent grakmaw = findPermanent(player1, "Grakmaw, Skyclave Ravager");
        assertThat(grakmaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gains a +1/+1 counter when another controlled creature with one dies")
    void gainsCounterWhenCreatureWithCounterDies() {
        Permanent grakmaw = addReadyGrakmaw();
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        dyingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        destroyWithAssassinate(dyingCreature);
        harness.passBothPriorities();

        assertThat(grakmaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not gain a counter when another controlled creature without one dies")
    void doesNotGainCounterWhenCreatureHasNoCounter() {
        Permanent grakmaw = addReadyGrakmaw();
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        destroyWithAssassinate(dyingCreature);
        harness.passBothPriorities();

        assertThat(grakmaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creates a base X/X black and green Hydra token from its +1/+1 counters")
    void deathCreatesHydraTokenWithCounterCountAsPowerAndToughness() {
        Permanent grakmaw = addReadyGrakmaw();
        grakmaw.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        grakmaw.setCounterCount(CounterType.CHARGE, 2);
        grakmaw.tap();

        destroyWithAssassinate(grakmaw);
        harness.passBothPriorities();

        List<Permanent> hydras = findPermanents(player1, "Hydra");
        assertThat(hydras).hasSize(1);
        Permanent hydra = hydras.getFirst();
        assertThat(hydra.getEffectivePower()).isEqualTo(5);
        assertThat(hydra.getEffectiveToughness()).isEqualTo(5);
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(hydra.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(hydra.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(hydra.getCard().getSubtypes()).contains(CardSubtype.HYDRA);
    }

    private Permanent addReadyGrakmaw() {
        Permanent grakmaw = addCreatureReady(player1, new GrakmawSkyclaveRavager());
        grakmaw.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        return grakmaw;
    }

    private void destroyWithAssassinate(Permanent target) {
        target.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }
}
