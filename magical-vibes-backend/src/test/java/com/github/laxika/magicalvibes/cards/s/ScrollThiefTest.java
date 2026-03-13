package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScrollThiefTest extends BaseCardTest {

    private Permanent addReadyCreature(Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Has ON_COMBAT_DAMAGE_TO_PLAYER DrawCardEffect")
    void hasCorrectEffect() {
        ScrollThief card = new ScrollThief();

        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst())
                .isInstanceOf(DrawCardEffect.class);
    }

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("Draws a card when dealing combat damage to a player")
    void drawsCardOnCombatDamage() {
        Permanent thief = addReadyCreature(new ScrollThief());
        thief.setAttacking(true);
        harness.setLife(player2, 20);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        // Player2 takes 1 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Player1 should have drawn a card
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    // ===== No trigger when blocked =====

    @Test
    @DisplayName("No trigger when Scroll Thief is blocked and killed")
    void noTriggerWhenBlocked() {
        Permanent thief = addReadyCreature(new ScrollThief());
        thief.setAttacking(true);

        // 4/4 blocker kills the 1/3 Scroll Thief
        Permanent blocker = new Permanent(new SerraAngel());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        // Scroll Thief should be dead
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Scroll Thief"));

        // No card drawn (didn't deal damage to a player)
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }
}
