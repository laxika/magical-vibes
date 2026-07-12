package com.github.laxika.magicalvibes.cards.h;

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
    }

    @Nested
    @DisplayName("Mode 1: Prevent the next 3 damage to any target")
    class PreventDamageMode {

        @Test
        @DisplayName("Adds a 3-damage prevention shield to a target creature")
        void shieldOnCreature() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getId().equals(targetId)).findFirst().orElseThrow();
            assertThat(bears.getDamagePreventionShield()).isEqualTo(3);
        }

        @Test
        @DisplayName("Adds a 3-damage prevention shield to a target player")
        void shieldOnPlayer() {
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 1, player2.getId());
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(3);
        }
    }
}
