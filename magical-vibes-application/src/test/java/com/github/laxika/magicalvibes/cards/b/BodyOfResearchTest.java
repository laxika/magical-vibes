package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BodyOfResearch.class, Forest.class, GrizzlyBears.class})
class BodyOfResearchTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Fractal with one counter for each card in the controller's library")
    void createsFractalWithCountersFromControllerLibrary() {
        harness.setLibrary(player1, List.of(
                new Forest(), new GrizzlyBears(), new Forest(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new BodyOfResearch()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent fractal = findPermanent(player1, "Fractal");
        assertThat(fractal.getCard().isToken()).isTrue();
        assertThat(fractal.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(fractal.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLUE);
        assertThat(fractal.getCard().getSubtypes()).containsExactly(CardSubtype.FRACTAL);
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(fractal.getEffectivePower()).isEqualTo(4);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("An empty library leaves the zero-toughness Fractal unable to survive")
    void emptyLibraryLeavesNoFractal() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new BodyOfResearch()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Fractal")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof BodyOfResearch);
    }

    @Test
    @DisplayName("Counts the library at resolution and later library changes do not change the counters")
    void libraryCountIsEvaluatedAtResolution() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new BodyOfResearch()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.passBothPriorities();

        Permanent fractal = findPermanent(player1, "Fractal");
        assertThat(countPermanents(player1, "Fractal")).isEqualTo(1);
        assertThat(countPermanents(player2, "Fractal")).isZero();
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.setLibrary(player1, List.of());
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(fractal.getEffectivePower()).isEqualTo(1);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(1);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 3);
    }
}
