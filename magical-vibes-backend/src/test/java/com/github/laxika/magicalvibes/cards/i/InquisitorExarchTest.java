package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InquisitorExarchTest extends BaseCardTest {

    @Test
    @DisplayName("Inquisitor Exarch has a ChooseOneEffect with two ETB options")
    void hasCorrectEffects() {
        InquisitorExarch card = new InquisitorExarch();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(ChooseOneEffect.class);
        ChooseOneEffect effect = (ChooseOneEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.options()).hasSize(2);
        assertThat(effect.options().get(0).effect()).isInstanceOf(GainLifeEffect.class);
        assertThat(effect.options().get(1).effect()).isInstanceOf(TargetPlayerLosesLifeEffect.class);
    }

    @Nested
    @DisplayName("Mode 1: You gain 2 life")
    class GainLifeMode {

        @Test
        @DisplayName("Controller gains 2 life")
        void controllerGains2Life() {
            castWithGainLifeMode();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        }

        @Test
        @DisplayName("Life gain works with non-default life totals")
        void lifeGainWithCustomTotals() {
            harness.setLife(player1, 5);

            castWithGainLifeMode();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(7);
        }

        @Test
        @DisplayName("Inquisitor Exarch enters the battlefield when choosing gain life mode")
        void exarchEntersBattlefield() {
            castWithGainLifeMode();
            harness.passBothPriorities(); // resolve creature

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Inquisitor Exarch"));
        }

        @Test
        @DisplayName("Stack is empty after full resolution")
        void stackIsEmptyAfterResolution() {
            castWithGainLifeMode();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.stack).isEmpty();
        }

        private void castWithGainLifeMode() {
            harness.setHand(player1, List.of(new InquisitorExarch()));
            harness.addMana(player1, ManaColor.WHITE, 2);
            harness.castCreature(player1, 0, 0);
        }
    }

    @Nested
    @DisplayName("Mode 2: Target opponent loses 2 life")
    class LoseLifeMode {

        @Test
        @DisplayName("Target opponent loses 2 life")
        void targetOpponentLoses2Life() {
            castWithLoseLifeMode();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        }

        @Test
        @DisplayName("Life loss works with non-default life totals")
        void lifeLossWithCustomTotals() {
            harness.setLife(player2, 5);

            castWithLoseLifeMode();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(3);
        }

        @Test
        @DisplayName("Inquisitor Exarch enters the battlefield when choosing lose life mode")
        void exarchEntersBattlefield() {
            castWithLoseLifeMode();
            harness.passBothPriorities(); // resolve creature

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Inquisitor Exarch"));
        }

        @Test
        @DisplayName("Stack is empty after full resolution")
        void stackIsEmptyAfterResolution() {
            castWithLoseLifeMode();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.stack).isEmpty();
        }

        private void castWithLoseLifeMode() {
            harness.setHand(player1, List.of(new InquisitorExarch()));
            harness.addMana(player1, ManaColor.WHITE, 2);
            harness.castCreature(player1, 0, 1, player2.getId());
        }
    }
}
