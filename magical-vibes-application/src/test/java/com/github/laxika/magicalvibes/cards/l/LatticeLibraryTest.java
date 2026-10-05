package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FanningTheFlames;
import com.github.laxika.magicalvibes.cards.f.Fracture;
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

@CardUsed({LatticeLibrary.class, FanningTheFlames.class, GrizzlyBears.class, Fracture.class})
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

        harness.castAndResolveSorcery(player1, 0, 1, player2.getId());

        assertThat(findPermanents(player1, "Fractal")).hasSize(1);
        assertThat(findPermanents(player1, "Fractal").getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.passBothPriorities();
        harness.castAndResolveSorcery(player1, 0, 1, player2.getId());

        assertThat(findPermanents(player1, "Fractal")).hasSize(1);
    }

    @Test
    void zeroStudyCountersCreateAFractalThatDies() {
        harness.setHand(player1, List.of(new LatticeLibrary()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castArtifact(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Lattice Library").getCounterCount(CounterType.STUDY)).isZero();
        assertThat(findPermanents(player1, "Fractal")).isEmpty();
    }

    @Test
    void entryTriggerUsesStudyCountersAtResolution() {
        harness.setHand(player1, List.of(new LatticeLibrary()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castArtifact(player1, 0, 2);
        harness.passBothPriorities();
        findPermanent(player1, "Lattice Library").setCounterCount(CounterType.STUDY, 5);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Fractal")).hasSize(1);
        assertThat(findPermanents(player1, "Fractal").getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void entryTriggerUsesLastKnownStudyCountersAfterLibraryIsDestroyed() {
        harness.setHand(player1, List.of(new LatticeLibrary()));
        harness.setHand(player2, List.of(new Fracture()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castArtifact(player1, 0, 2);
        harness.passBothPriorities();
        Permanent library = findPermanent(player1, "Lattice Library");
        harness.castAndResolveInstant(player2, 0, library.getId());
        harness.assertInGraveyard(player1, "Lattice Library");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Fractal")).hasSize(1);
        assertThat(findPermanents(player1, "Fractal").getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void spellCastTriggerUsesLastKnownStudyCountersAfterLibraryIsDestroyed() {
        Permanent library = harness.addToBattlefieldAndReturn(player1, new LatticeLibrary());
        library.setCounterCount(CounterType.STUDY, 3);
        harness.setHand(player1, List.of(new LatticeLibrary()));
        harness.setHand(player2, List.of(new Fracture()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castArtifact(player1, 0, 1);
        harness.castAndResolveInstant(player2, 0, library.getId());
        harness.assertInGraveyard(player1, "Lattice Library");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Fractal")).hasSize(1);
        assertThat(findPermanents(player1, "Fractal").getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void zeroXSpellStillTriggersAndUsesTheLibrarysStudyCounters() {
        Permanent library = harness.addToBattlefieldAndReturn(player1, new LatticeLibrary());
        library.setCounterCount(CounterType.STUDY, 3);
        harness.setHand(player1, List.of(new LatticeLibrary()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castArtifact(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Fractal")).hasSize(1);
        assertThat(findPermanents(player1, "Fractal").getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Fractal")).hasSize(1);
    }

    @Test
    void castingLibraryItselfConsumesTheFirstXSpellOfTheTurn() {
        harness.setHand(player1, List.of(new LatticeLibrary(), new LatticeLibrary()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castArtifact(player1, 0, 2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castArtifact(player1, 0, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Fractal")).hasSize(2);
        assertThat(findPermanents(player1, "Fractal")
                .stream().map(p -> p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)))
                .containsExactlyInAnyOrder(2, 1);
    }
}
