package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ForbiddingSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Charges two mana for each creature attacking its controller")
    void chargesTwoManaForEachAttacker() {
        Permanent bear1 = addCreatureReady(player2, new GrizzlyBears());
        Permanent bear2 = addCreatureReady(player2, new GrizzlyBears());
        castSpirit();

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        declareAttackers(player2, List.of(
                gd.playerBattlefields.get(player2.getId()).indexOf(bear1),
                gd.playerBattlefields.get(player2.getId()).indexOf(bear2)));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Rejects an attack without enough mana for every attacker")
    void rejectsInsufficientAttackTax() {
        Permanent bear1 = addCreatureReady(player2, new GrizzlyBears());
        Permanent bear2 = addCreatureReady(player2, new GrizzlyBears());
        castSpirit();

        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(
                gd.playerBattlefields.get(player2.getId()).indexOf(bear1),
                gd.playerBattlefields.get(player2.getId()).indexOf(bear2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("Survives cleanup and expires at its controller's next turn")
    void lastsUntilControllerNextTurn() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        castSpirit();

        gd.expireEndOfTurnFloatingEffects();
        assertThatThrownBy(() -> declareAttackers(player2, List.of(
                gd.playerBattlefields.get(player2.getId()).indexOf(bear))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");

        gd.expireFloatingEffectsAtTurnStart(player1.getId());
        assertThatCode(() -> declareAttackers(player2, List.of(
                gd.playerBattlefields.get(player2.getId()).indexOf(bear))))
                .doesNotThrowAnyException();
    }

    private void castSpirit() {
        harness.setHand(player1, List.of(new ForbiddingSpirit()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
