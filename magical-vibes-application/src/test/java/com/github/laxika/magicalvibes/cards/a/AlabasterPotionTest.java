package com.github.laxika.magicalvibes.cards.a;

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

@CardUsed({AlabasterPotion.class, GrizzlyBears.class})
class AlabasterPotionTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Target player gains X life")
    class GainLifeMode {

        @Test
        @DisplayName("Target player gains X life for X paid")
        void targetPlayerGainsXLife() {
            harness.setHand(player1, List.of(new AlabasterPotion()));
            harness.addMana(player1, ManaColor.WHITE, 5);
            int before = gd.playerLifeTotals.get(player2.getId());

            harness.castModalInstantForX(player1, 0, 0, 3, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(before + 3);
        }

        @Test
        @DisplayName("Cannot target a creature with the gain-life mode")
        void cannotTargetCreature() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new AlabasterPotion()));
            harness.addMana(player1, ManaColor.WHITE, 5);

            assertThatThrownBy(() -> harness.castModalInstantForX(player1, 0, 0, 3, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Prevent the next X damage to any target")
    class PreventDamageMode {

        @Test
        @DisplayName("Adds an X-damage prevention shield to a target creature")
        void shieldOnCreature() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new AlabasterPotion()));
            harness.addMana(player1, ManaColor.WHITE, 5);

            harness.castModalInstantForX(player1, 0, 1, 2, bears.getId());
            harness.passBothPriorities();

            assertThat(bears.getDamagePreventionShield()).isEqualTo(2);
        }

        @Test
        @DisplayName("Adds an X-damage prevention shield to a target player")
        void shieldOnPlayer() {
            harness.setHand(player1, List.of(new AlabasterPotion()));
            harness.addMana(player1, ManaColor.WHITE, 5);

            harness.castModalInstantForX(player1, 0, 1, 3, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(3);
        }

        @Test
        @DisplayName("Prevents combat damage to the targeted player")
        void preventsCombatDamageToTargetPlayer() {
            harness.setLife(player2, 20);
            Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new AlabasterPotion()));
            harness.addMana(player1, ManaColor.WHITE, 5);

            harness.castModalInstantForX(player1, 0, 1, 2, player2.getId());
            harness.passBothPriorities();

            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
            resolveCombat();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
            assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
        }
    }
}
