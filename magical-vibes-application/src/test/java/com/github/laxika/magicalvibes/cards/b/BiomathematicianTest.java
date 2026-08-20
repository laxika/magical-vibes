package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BiomathematicianTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a 1/1 Fractal token")
    void enteringCreatesFractalTokenWithCounter() {
        harness.setHand(player1, List.of(new Biomathematician()));
        addManaForBiomathematician();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent firstFractal = findFractals().getFirst();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> fractals = findFractals();
        assertThat(fractals).hasSize(2);
        assertThat(firstFractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(fractals.stream()
                .filter(fractal -> fractal != firstFractal)
                .findFirst()
                .orElseThrow()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
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
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && "Fractal".equals(permanent.getCard().getName()))
                .toList();
    }
}
