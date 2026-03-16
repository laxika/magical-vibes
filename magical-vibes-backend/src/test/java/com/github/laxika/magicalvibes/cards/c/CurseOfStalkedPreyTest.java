package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CurseOfStalkedPreyTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Is an enchant-player aura with PutCountersOnSourceEffect")
    void hasCorrectEffects() {
        CurseOfStalkedPrey card = new CurseOfStalkedPrey();

        assertThat(card.isEnchantPlayer()).isTrue();

        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst())
                .isInstanceOf(PutCountersOnSourceEffect.class);
    }

    // ===== Casting and attaching to player =====

    @Test
    @DisplayName("Can be cast targeting opponent, enters battlefield attached to that player")
    void castTargetingOpponent() {
        harness.setHand(player1, List.of(new CurseOfStalkedPrey()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Curse of Stalked Prey"));

        Permanent curse = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Curse of Stalked Prey"))
                .findFirst().orElseThrow();
        assertThat(curse.getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Can be cast targeting self")
    void castTargetingSelf() {
        harness.setHand(player1, List.of(new CurseOfStalkedPrey()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, player1.getId());
        harness.passBothPriorities(); // resolve

        Permanent curse = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Curse of Stalked Prey"))
                .findFirst().orElseThrow();
        assertThat(curse.getAttachedTo()).isEqualTo(player1.getId());
    }

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("Creature gets +1/+1 counter when dealing combat damage to enchanted player")
    void creatureGetsCounterOnCombatDamage() {
        // Put curse on player2
        Permanent curse = addCurseOnPlayer2();

        // Add an attacking creature for player1
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Player2 takes 2 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Attacker should have a +1/+1 counter
        assertThat(attacker.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple creatures each get a +1/+1 counter when dealing combat damage to enchanted player")
    void multipleCreaturesGetCounters() {
        Permanent curse = addCurseOnPlayer2();

        Permanent attacker1 = new Permanent(new GrizzlyBears());
        attacker1.setSummoningSick(false);
        attacker1.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker1);

        Permanent attacker2 = new Permanent(new GrizzlyBears());
        attacker2.setSummoningSick(false);
        attacker2.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker2);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Player2 takes 4 total damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        // Resolve both triggered abilities
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(attacker1.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(attacker2.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocked creature that deals no damage to player does not get a counter")
    void blockedCreatureDoesNotGetCounter() {
        Permanent curse = addCurseOnPlayer2();

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        // Block with a creature (attacker is at battlefield index 1, curse is at index 0)
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Player2 takes no damage (creature was blocked)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        // No counter on attacker (it dealt damage to blocker, not player)
        assertThat(attacker.getPlusOnePlusOneCounters()).isEqualTo(0);
    }

    @Test
    @DisplayName("Curse is not removed as orphaned aura when attached to player")
    void curseNotRemovedAsOrphanedAura() {
        Permanent curse = addCurseOnPlayer2();

        // Advance through several steps to trigger SBA / orphan aura checks
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Curse should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Curse of Stalked Prey"));
    }

    @Test
    @DisplayName("Counter stacks — creature with existing counter gets another")
    void counterStacks() {
        Permanent curse = addCurseOnPlayer2();

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setPlusOnePlusOneCounters(1); // already has a counter
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // 2 base + 1 counter = 3 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);

        // Resolve triggered ability
        harness.passBothPriorities();

        assertThat(attacker.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    // ===== Helpers =====

    private Permanent addCurseOnPlayer2() {
        CurseOfStalkedPrey curseCard = new CurseOfStalkedPrey();
        Permanent curse = new Permanent(curseCard);
        curse.setAttachedTo(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(curse);
        return curse;
    }
}
