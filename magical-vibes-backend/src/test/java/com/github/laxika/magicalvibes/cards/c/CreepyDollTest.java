package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.MahamotiDjinn;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreepyDollTest extends BaseCardTest {

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    @Test
    @DisplayName("Has FlipCoinWinEffect wrapping DestroyTargetPermanentEffect on combat damage to creature")
    void hasCorrectEffects() {
        CreepyDoll card = new CreepyDoll();

        var effects = card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(FlipCoinWinEffect.class);

        FlipCoinWinEffect flipEffect = (FlipCoinWinEffect) effects.getFirst();
        assertThat(flipEffect.wrapped()).isInstanceOf(DestroyTargetPermanentEffect.class);
    }

    @Test
    @DisplayName("Combat damage to creature triggers coin flip — blocker destroyed on win or survives on loss")
    void combatDamageToCreatureTriggersFlip() {
        Permanent creepyDoll = addReadyCreature(player1, new CreepyDoll());
        creepyDoll.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new MahamotiDjinn());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(gd.gameLog).anyMatch(log -> log.contains("Creepy Doll's ability triggers"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("coin flip for Creepy Doll"));

        boolean inGraveyard = gd.playerGraveyards.get(player2.getId()).stream()
                .anyMatch(c -> c.getName().equals("Mahamoti Djinn"));
        boolean onBattlefield = gd.playerBattlefields.get(player2.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Mahamoti Djinn"));

        if (inGraveyard) {
            // Won the flip: blocker destroyed
            assertThat(onBattlefield).isFalse();
            assertThat(gd.gameLog).anyMatch(log -> log.contains("wins the coin flip"));
        } else {
            // Lost the flip: blocker survives
            assertThat(onBattlefield).isTrue();
            assertThat(gd.gameLog).anyMatch(log -> log.contains("loses the coin flip"));
        }
    }

    @Test
    @DisplayName("Creepy Doll survives combat due to indestructible")
    void survivesCombarDueToIndestructible() {
        Permanent creepyDoll = addReadyCreature(player1, new CreepyDoll());
        creepyDoll.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new MahamotiDjinn());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage
        harness.passBothPriorities(); // resolve triggered ability

        // Creepy Doll should still be on the battlefield (indestructible)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Creepy Doll"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Creepy Doll"));
    }

    @Test
    @DisplayName("No trigger fires when Creepy Doll deals combat damage to a player")
    void noTriggerOnDamageToPlayer() {
        Permanent creepyDoll = addReadyCreature(player1, new CreepyDoll());
        creepyDoll.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Should deal 1 damage to player, no trigger
        assertThat(gd.gameLog).noneMatch(log -> log.contains("Creepy Doll's ability triggers"));
        assertThat(gd.gameLog).noneMatch(log -> log.contains("coin flip"));
    }
}
