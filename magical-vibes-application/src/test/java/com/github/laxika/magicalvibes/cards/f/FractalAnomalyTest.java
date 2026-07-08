package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateFractalTokenWithCountersFromCardsDrawnThisTurnEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FractalAnomalyTest extends BaseCardTest {

    @Test
    @DisplayName("Has a single Fractal-token effect")
    void hasCorrectStructure() {
        FractalAnomaly card = new FractalAnomaly();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(CreateFractalTokenWithCountersFromCardsDrawnThisTurnEffect.class);
    }

    @Test
    @DisplayName("Creates a Fractal with +1/+1 counters equal to cards drawn this turn")
    void createsFractalWithCounters() {
        gd.cardsDrawnThisTurn.put(player1.getId(), 3);

        harness.setHand(player1, List.of(new FractalAnomaly()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent fractal = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fractal")).findFirst().orElseThrow();
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(fractal.getEffectivePower()).isEqualTo(3);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("With no cards drawn, the 0/0 Fractal dies to state-based actions")
    void zeroCountersFractalDies() {
        gd.cardsDrawnThisTurn.put(player1.getId(), 0);

        harness.setHand(player1, List.of(new FractalAnomaly()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fractal"));
    }
}
