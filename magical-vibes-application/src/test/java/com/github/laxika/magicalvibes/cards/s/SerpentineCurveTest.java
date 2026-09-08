package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SerpentineCurveTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Fractal with counters for owned instants and sorceries in the graveyard and exile")
    void createsFractalWithCountersFromGraveyardAndExile() {
        harness.setHand(player1, List.of(new SerpentineCurve()));
        harness.setGraveyard(player1, List.of(new Opt(), new ThinkTwice(), new GrizzlyBears()));
        harness.setExile(player1, List.of(new Opt(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Opt(), new ThinkTwice()));
        harness.setExile(player2, List.of(new ThinkTwice()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent fractal = findFractal();
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(fractal.getEffectivePower()).isEqualTo(4);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("With no owned instant or sorcery cards, creates a 1/1 Fractal")
    void createsOneOneFractalWithNoMatchingCards() {
        harness.setHand(player1, List.of(new SerpentineCurve()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setExile(player1, List.of(new GrizzlyBears()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent fractal = findFractal();
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(fractal.getEffectivePower()).isEqualTo(1);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(1);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent findFractal() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && "Fractal".equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }
}
