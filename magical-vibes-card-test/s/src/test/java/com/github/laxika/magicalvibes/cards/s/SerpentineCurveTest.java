package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Curate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerpentineCurve.class, Curate.class, ScurridColony.class})
class SerpentineCurveTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Fractal with counters for owned instants and sorceries in the graveyard and exile")
    void createsFractalWithCountersFromGraveyardAndExile() {
        harness.setHand(player1, List.of(new SerpentineCurve()));
        harness.setGraveyard(player1, List.of(new Curate(), new Curate(), new ScurridColony()));
        harness.setExile(player1, List.of(new Curate(), new ScurridColony()));
        harness.setGraveyard(player2, List.of(new Curate(), new Curate()));
        harness.setExile(player2, List.of(new Curate()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent fractal = findFractal();
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(fractal.getEffectivePower()).isEqualTo(4);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("With no owned instant or sorcery cards, creates a 1/1 Fractal")
    void createsOneOneFractalWithNoMatchingCards() {
        harness.setHand(player1, List.of(new SerpentineCurve()));
        harness.setGraveyard(player1, List.of(new ScurridColony()));
        harness.setExile(player1, List.of(new ScurridColony()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent fractal = findFractal();
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(fractal.getEffectivePower()).isEqualTo(1);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts sorceries in both zones and keeps the counters after the zones change")
    void countsSorceriesAndKeepsCountersAfterResolution() {
        harness.setHand(player1, List.of(new SerpentineCurve()));
        harness.setGraveyard(player1, List.of(new SerpentineCurve()));
        harness.setExile(player1, List.of(new SerpentineCurve()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent fractal = findFractal();
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of());
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(fractal.getEffectivePower()).isEqualTo(3);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Face-down exiled cards do not contribute to the counter count")
    void excludesFaceDownExiledCards() {
        harness.setHand(player1, List.of(new SerpentineCurve()));
        harness.setExile(player1, List.of(new Curate()));
        gd.addToExile(player1.getId(), new Curate(), null, true);
        gd.addToExile(player1.getId(), new SerpentineCurve(), null, true);
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findFractal().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts matching cards when the spell resolves rather than when it is cast")
    void countsCardsAtResolution() {
        harness.setHand(player1, List.of(new SerpentineCurve()));
        harness.setGraveyard(player1, List.of(new Curate()));
        addMana();
        harness.castSorcery(player1, 0, 0);

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(new Curate(), new SerpentineCurve()));
        harness.passBothPriorities();

        assertThat(findFractal().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
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
