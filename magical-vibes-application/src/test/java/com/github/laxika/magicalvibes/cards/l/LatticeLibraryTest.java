package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FanningTheFlames;
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

@CardUsed({LatticeLibrary.class, FanningTheFlames.class, GrizzlyBears.class})
class LatticeLibraryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X study counters and creates a matching Fractal")
    void entersWithStudyCountersAndCreatesFractal() {
        harness.setHand(player1, List.of(new LatticeLibrary()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castArtifact(player1, 0, 2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent library = findPermanent(player1, "Lattice Library");
        assertThat(library.getCounterCount(CounterType.STUDY)).isEqualTo(2);

        Permanent fractal = findPermanents(player1, "Fractal").getFirst();
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(fractal.getEffectivePower()).isEqualTo(2);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creates a Fractal after the first X spell each turn")
    void createsFractalForFirstXSpellEachTurn() {
        Permanent library = harness.addToBattlefieldAndReturn(player1, new LatticeLibrary());
        library.setCounterCount(CounterType.STUDY, 2);
        harness.setHand(player1, List.of(new GrizzlyBears(), new FanningTheFlames(), new FanningTheFlames()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.RED, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Fractal")).hasSize(1);
        assertThat(findPermanents(player1, "Fractal").getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.passBothPriorities();
        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Fractal")).hasSize(1);
    }
}
