package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesXLifeAndControllerGainsLifeLostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExsanguinateTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has correct effect type and does not need a target")
    void hasCorrectProperties() {
        Exsanguinate card = new Exsanguinate();

        assertThat(EffectResolution.needsTarget(card)).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(EachOpponentLosesXLifeAndControllerGainsLifeLostEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Exsanguinate puts it on the stack with correct X value")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new Exsanguinate()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 3);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Exsanguinate");
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Opponent loses X life and controller gains X life")
    void opponentLosesXLifeControllerGainsXLife() {
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Exsanguinate()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, 5);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("X=0 does nothing")
    void xZeroDoesNothing() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Exsanguinate()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can pay X with any color mana (no X color restriction)")
    void canPayXWithAnyColor() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Exsanguinate()));
        // {X}{B}{B} with X=3: need 2 black + 3 any = 5 total
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Cannot cast without enough mana for base cost {B}{B}")
    void cannotCastWithoutBaseMana() {
        harness.setHand(player1, List.of(new Exsanguinate()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exsanguinate goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new Exsanguinate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Exsanguinate"));
    }

    @Test
    @DisplayName("Life loss can bring opponent below zero")
    void lifeLossCanBringBelowZero() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 3);
        harness.setHand(player1, List.of(new Exsanguinate()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, 5);
        harness.passBothPriorities();

        // Opponent goes to -2 life (3 - 5)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(-2);
        // Controller gains 5 life (the full X, not capped by opponent's remaining life)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }
}
