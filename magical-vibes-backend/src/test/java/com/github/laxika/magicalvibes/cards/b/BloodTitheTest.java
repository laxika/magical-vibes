package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeAndControllerGainsLifeLostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodTitheTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has correct effect type and does not need a target")
    void hasCorrectProperties() {
        BloodTithe card = new BloodTithe();

        assertThat(EffectResolution.needsTarget(card)).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(EachOpponentLosesLifeAndControllerGainsLifeLostEffect.class);
        EachOpponentLosesLifeAndControllerGainsLifeLostEffect effect =
                (EachOpponentLosesLifeAndControllerGainsLifeLostEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.amount()).isEqualTo(3);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Blood Tithe puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new BloodTithe()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Blood Tithe");
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Each opponent loses 3 life and controller gains life equal to life lost")
    void opponentLoses3LifeControllerGains3Life() {
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BloodTithe()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Blood Tithe goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new BloodTithe()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blood Tithe"));
    }

    @Test
    @DisplayName("Life loss can bring opponent below zero and controller still gains the full amount")
    void lifeLossCanBringBelowZero() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 2);
        harness.setHand(player1, List.of(new BloodTithe()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Opponent goes to -1 life (2 - 3)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(-1);
        // Controller gains 3 life (the full amount lost, not capped)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }
}
