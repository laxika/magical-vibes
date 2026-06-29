package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.LifeTotalCantChangeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlatinumEmperionTest extends BaseCardTest {

    @Test
    @DisplayName("Has LifeTotalCantChangeEffect as static effect")
    void hasStaticEffect() {
        PlatinumEmperion card = new PlatinumEmperion();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(LifeTotalCantChangeEffect.class);
    }

    @Test
    @DisplayName("Damage does not change controller's life total")
    void damageDoesNotChangeLife() {
        harness.addToBattlefield(player1, new PlatinumEmperion());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Damage changes life normally after Platinum Emperion is removed")
    void damageChangesLifeAfterRemoval() {
        harness.addToBattlefield(player1, new PlatinumEmperion());
        harness.setLife(player1, 20);

        // Remove Platinum Emperion
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Opponent's Platinum Emperion does not protect you")
    void opponentEmperionDoesNotProtect() {
        harness.addToBattlefield(player2, new PlatinumEmperion());
        harness.setLife(player1, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Life total stays unchanged even when it would drop to 0")
    void lifeStaysUnchangedAtLethalDamage() {
        harness.addToBattlefield(player1, new PlatinumEmperion());
        harness.setLife(player1, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        // Life total stays at 1 — the damage doesn't change it
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Life gain is also prevented")
    void lifeGainPrevented() {
        harness.addToBattlefield(player1, new PlatinumEmperion());
        harness.setLife(player1, 10);

        // Directly test via the query service
        assertThat(gqs.canPlayerLifeChange(gd, player1.getId())).isFalse();
        assertThat(gqs.canPlayerLifeChange(gd, player2.getId())).isTrue();
    }
}
