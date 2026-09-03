package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HealingSalve.class, GrizzlyBears.class})
class HealingSalveTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Target player gains 3 life")
    class GainLifeMode {

        @Test
        @DisplayName("Target player gains 3 life")
        void targetPlayerGainsLife() {
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            int before = gd.playerLifeTotals.get(player2.getId());

            harness.castInstant(player1, 0, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(before + 3);
        }

        @Test
        @DisplayName("Cannot target a creature with the gain-life mode")
        void cannotTargetCreature() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Prevent the next 3 damage to any target")
    class PreventDamageMode {

        @Test
        @DisplayName("Adds a 3-damage prevention shield to a target creature")
        void shieldOnCreature() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 1, bears.getId());
            harness.passBothPriorities();

            assertThat(bears.getDamagePreventionShield()).isEqualTo(3);
        }

        @Test
        @DisplayName("Adds a 3-damage prevention shield to a target player")
        void shieldOnPlayer() {
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 1, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(3);
        }

        @Test
        @DisplayName("Prevents only the next 3 damage to a target player")
        void preventsOnlyNextThreeDamage() {
            harness.setLife(player2, 20);
            Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
            Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 1, player2.getId());
            harness.passBothPriorities();

            declareAttackers(List.of(
                    gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker),
                    gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker)));
            resolveCombat();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
            assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
        }
    }
}
