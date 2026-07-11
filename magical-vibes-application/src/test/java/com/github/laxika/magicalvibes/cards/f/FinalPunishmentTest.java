package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FinalPunishmentTest extends BaseCardTest {

    @Test
    @DisplayName("Target loses life equal to the damage dealt to them this turn")
    void losesLifeEqualToDamageThisTurn() {
        shockPlayer(player2.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        castFinalPunishment(player2.getId());

        // 2 damage dealt this turn -> loses 2 more life (18 -> 16)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Accumulates damage from multiple sources this turn")
    void accumulatesDamageFromMultipleSources() {
        shockPlayer(player2.getId());
        shockPlayer(player2.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        castFinalPunishment(player2.getId());

        // 4 damage dealt this turn -> loses 4 more life (16 -> 12)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Target that took no damage loses no life")
    void noDamageMeansNoLifeLoss() {
        shockPlayer(player2.getId());

        // Final Punishment targets player1, who took no damage this turn
        castFinalPunishment(player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private void shockPlayer(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }

    private void castFinalPunishment(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new FinalPunishment()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }
}
