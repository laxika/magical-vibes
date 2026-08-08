package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DebtToTheDeathlessTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent loses two times X life and controller gains that much life")
    void opponentLosesTwiceXControllerGains() {
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new DebtToTheDeathless()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("X=0 does nothing")
    void xZeroDoesNothing() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new DebtToTheDeathless()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Life loss can bring opponent below zero and controller still gains the full amount")
    void lifeLossCanBringBelowZero() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 3);
        harness.setHand(player1, List.of(new DebtToTheDeathless()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(-3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Cannot cast without enough mana for the base cost {W}{W}{B}{B}")
    void cannotCastWithoutBaseMana() {
        harness.setHand(player1, List.of(new DebtToTheDeathless()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new DebtToTheDeathless()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Debt to the Deathless");
    }
}
