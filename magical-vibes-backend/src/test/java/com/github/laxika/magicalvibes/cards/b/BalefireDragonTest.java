package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class BalefireDragonTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has ON_COMBAT_DAMAGE_TO_PLAYER effect")
    void hasCombatDamageToPlayerEffect() {
        BalefireDragon card = new BalefireDragon();

        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER))
                .hasSize(1)
                .first()
                .isInstanceOf(DealDamageToEachCreatureDamagedPlayerControlsEffect.class);
    }

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("Deals 6 damage to each creature the damaged player controls when dealing combat damage")
    void dealsDamageToEachCreatureOnCombatDamage() {
        harness.setLife(player2, 20);
        Permanent dragon = addBalefireDragonReady(player1);
        dragon.setAttacking(true);

        // Opponent has two 2/2 creatures on the battlefield (not blocking)
        Permanent bear1 = addReadyCreature(player2);
        Permanent bear2 = addReadyCreature(player2);

        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>());

        resolveCombat();

        // Dragon deals 6 combat damage to player2
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);

        // Both 2/2 bears should be destroyed (6 damage each)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(bear1, bear2);
    }

    @Test
    @DisplayName("Deals 6 damage to a high-toughness creature that survives")
    void highToughnessCreatureSurvives() {
        harness.setLife(player2, 20);
        Permanent dragon = addBalefireDragonReady(player1);
        dragon.setAttacking(true);

        // Opponent has a creature with 7 toughness (survives 6 damage)
        Permanent wall = addCreatureReady(player2, new SerraAngel()); // 4/4 — will be destroyed

        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>());

        resolveCombat();

        // Dragon deals 6 combat damage to player2
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);

        // Serra Angel (4/4) takes 6 damage and should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(wall);
    }

    @Test
    @DisplayName("No trigger when dragon is blocked and deals no player damage")
    void noTriggerWhenBlocked() {
        harness.setLife(player2, 20);
        Permanent dragon = addBalefireDragonReady(player1);
        dragon.setAttacking(true);

        // Blocker with enough toughness to survive
        Permanent blocker = addCreatureReady(player2, new SerraAngel()); // 4/4
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        // Another creature that should NOT be damaged
        Permanent bear = addReadyCreature(player2);

        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>());

        resolveCombat();

        // No combat damage to player
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        // Bear should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
    }

    @Test
    @DisplayName("Does not damage creatures the attacker controls")
    void doesNotDamageAttackerCreatures() {
        harness.setLife(player2, 20);
        Permanent dragon = addBalefireDragonReady(player1);
        dragon.setAttacking(true);

        // Attacker's own creature should not be affected
        Permanent ownBear = addReadyCreature(player1);

        // Opponent has a creature
        Permanent oppBear = addReadyCreature(player2);

        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>());

        resolveCombat();

        // Attacker's creature should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownBear);

        // Opponent's creature should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(oppBear);
    }

    @Test
    @DisplayName("Trigger still fires when opponent has no creatures")
    void triggerFiresWithNoOpponentCreatures() {
        harness.setLife(player2, 20);
        Permanent dragon = addBalefireDragonReady(player1);
        dragon.setAttacking(true);

        // No creatures for opponent
        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>());

        resolveCombat();

        // Just combat damage to player
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    // ===== Helpers =====

    private Permanent addBalefireDragonReady(Player player) {
        Permanent perm = new Permanent(new BalefireDragon());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }


    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
