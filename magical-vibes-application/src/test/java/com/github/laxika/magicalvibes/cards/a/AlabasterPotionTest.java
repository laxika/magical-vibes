package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
    }

    @Nested
    @DisplayName("Mode 1: Prevent the next X damage to any target")
    class PreventDamageMode {

        @Test
        @DisplayName("Adds an X-damage prevention shield to a target creature")
        void shieldOnCreature() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new AlabasterPotion()));
            harness.addMana(player1, ManaColor.WHITE, 5);

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castModalInstantForX(player1, 0, 1, 2, targetId);
            harness.passBothPriorities();

            Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getId().equals(targetId)).findFirst().orElseThrow();
            assertThat(bears.getDamagePreventionShield()).isEqualTo(2);
        }

        @Test
        @DisplayName("Adds an X-damage prevention shield to a target player")
        void shieldOnPlayer() {
            harness.setHand(player1, List.of(new AlabasterPotion()));
            harness.addMana(player1, ManaColor.WHITE, 5);

            harness.castModalInstantForX(player1, 0, 1, 3, player2.getId());
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(3);
        }
    }
}
