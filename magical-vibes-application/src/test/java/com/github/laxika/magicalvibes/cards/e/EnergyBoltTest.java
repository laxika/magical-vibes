package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EnergyBoltTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: deals X damage to target player or planeswalker")
    class DamageMode {

        @Test
        @DisplayName("Deals X damage to the targeted player")
        void dealsXDamageToPlayer() {
            harness.setHand(player1, List.of(new EnergyBolt()));
            harness.addMana(player1, ManaColor.RED, 5);
            harness.addMana(player1, ManaColor.WHITE, 1);
            int before = gd.playerLifeTotals.get(player2.getId());

            harness.castModalInstantForX(player1, 0, 0, 3, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(before - 3);
        }

        @Test
        @DisplayName("X = 0 deals no damage")
        void zeroXDealsNoDamage() {
            harness.setHand(player1, List.of(new EnergyBolt()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.addMana(player1, ManaColor.WHITE, 1);
            int before = gd.playerLifeTotals.get(player2.getId());

            harness.castModalInstantForX(player1, 0, 0, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(before);
        }
    }

    @Nested
    @DisplayName("Mode 1: target player gains X life")
    class LifeGainMode {

        @Test
        @DisplayName("Targeted player gains X life")
        void targetPlayerGainsXLife() {
            harness.setHand(player1, List.of(new EnergyBolt()));
            harness.addMana(player1, ManaColor.RED, 5);
            harness.addMana(player1, ManaColor.WHITE, 1);
            int before = gd.playerLifeTotals.get(player1.getId());

            harness.castModalInstantForX(player1, 0, 1, 4, player1.getId());
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(before + 4);
        }
    }
}
