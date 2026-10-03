package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.r.Resculpt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Biomathematician.class, Resculpt.class})
class BiomathematicianTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a 1/1 Fractal token")
    void enteringCreatesFractalTokenWithCounter() {
        harness.setHand(player1, List.of(new Biomathematician()));
        addManaForBiomathematician();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent fractal = findFractals().getFirst();
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(fractal.getEffectivePower()).isEqualTo(1);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Entering puts a counter on each Fractal you control, including existing ones")
    void enteringCountersAllControlledFractals() {
        harness.setHand(player1, List.of(new Biomathematician(), new Biomathematician()));
        addManaForTwoBiomathematicians();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent firstFractal = findFractals().getFirst();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> fractals = findFractals();
        assertThat(fractals).hasSize(2);
        assertThat(firstFractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(fractals.stream()
                .filter(fractal -> fractal != firstFractal)
                .findFirst()
                .orElseThrow()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only controlled Fractals receive counters")
    void enteringDoesNotCounterOpponentsFractalsOrNonFractals() {
        harness.enterBattlefieldAndReturn(player2, new Biomathematician());
        resolveAllTriggers();
        Permanent opponentFractal = findPermanent(player2, "Fractal");

        harness.setHand(player1, List.of(new Biomathematician()));
        addManaForBiomathematician();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(opponentFractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Fractal").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Biomathematician").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player2, "Biomathematician").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The trigger creates and grows a Fractal even after its source leaves")
    void triggerResolvesAfterSourceIsExiled() {
        harness.setHand(player1, List.of(new Biomathematician(), new Resculpt()));
        addManaForBiomathematician();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Biomathematician");
        harness.castAndResolveInstant(player1, 0, source.getId());
        harness.assertNotOnBattlefield(player1, "Biomathematician");
        assertThat(findFractals()).isEmpty();
        resolveAllTriggers();

        assertThat(findFractals()).hasSize(1);
        Permanent fractal = findFractals().getFirst();
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(1);
        assertThat(findPermanent(player1, "Elemental").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void addManaForBiomathematician() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addManaForTwoBiomathematicians() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private List<Permanent> findFractals() {
        return findPermanents(player1, "Fractal");
    }
}
