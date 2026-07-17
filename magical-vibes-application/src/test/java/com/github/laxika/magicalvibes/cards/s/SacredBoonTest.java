package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SacredBoonTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Sacred Boon targets a creature and goes on the stack")
    void castingPutsItOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SacredBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getCard().getName()).isEqualTo("Sacred Boon");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Sacred Boon adds a 3-damage prevention shield to the target creature")
    void resolvingAddsShield() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SacredBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = bears(player1);
        assertThat(bears.getDamageToCounterPreventionShield()).isEqualTo(3);
    }

    @Test
    @DisplayName("Prevented noncombat damage becomes +0/+1 counters at the next end step")
    void preventedDamageBecomesCountersAtEndStep() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SacredBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        // Shock deals 2 damage to the shielded creature — fully prevented.
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = bears(player1);
        // Prevented, so still alive; 1 of the 3 shield remains; no counters until end step.
        assertThat(bears.getDamageToCounterPreventionShield()).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isZero();

        advanceToEndStep(player1);
        resolveAllDelayedTriggers();

        Permanent afterEnd = bears(player1);
        assertThat(afterEnd.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(2);
        // +0/+1 counters add toughness only.
        assertThat(gqs.getEffectiveToughness(gd, afterEnd)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, afterEnd)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prevented combat damage becomes +0/+1 counters at the next end step")
    void preventedCombatDamageBecomesCounters() {
        GrizzlyBears defenderCard = new GrizzlyBears();
        Permanent defender = new Permanent(defenderCard);
        defender.setSummoningSick(false);
        defender.setDamageToCounterPreventionShield(3);
        defender.setBlocking(true);
        defender.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(defender);

        GrizzlyBears attackerCard = new GrizzlyBears();
        Permanent attacker = new Permanent(attackerCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Defender took 2 prevented combat damage → survives, 1 shield remaining.
        Permanent survivor = bears(player2);
        assertThat(survivor.getDamageToCounterPreventionShield()).isEqualTo(1);

        advanceToEndStep(player1);
        resolveAllDelayedTriggers();

        assertThat(bears(player2).getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("No counters are added when no damage is prevented")
    void noCountersWhenNoDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SacredBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        advanceToEndStep(player1);
        resolveAllDelayedTriggers();

        assertThat(bears(player1).getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Sacred Boon shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        GrizzlyBears bear = new GrizzlyBears();
        Permanent perm = new Permanent(bear);
        perm.setSummoningSick(false);
        perm.setDamageToCounterPreventionShield(3);
        gd.playerBattlefields.get(player1.getId()).add(perm);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears(player1).getDamageToCounterPreventionShield()).isZero();
    }

    @Test
    @DisplayName("Sacred Boon cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new SacredBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent bears(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void resolveAllDelayedTriggers() {
        int safety = 0;
        while (!gd.stack.isEmpty() && safety < 20) {
            harness.passBothPriorities();
            safety++;
        }
    }
}
