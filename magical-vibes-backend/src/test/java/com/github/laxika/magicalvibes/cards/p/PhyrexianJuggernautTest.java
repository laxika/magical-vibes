package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhyrexianJuggernautTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Phyrexian Juggernaut has MustAttackEffect as static effect")
    void hasCorrectEffects() {
        PhyrexianJuggernaut card = new PhyrexianJuggernaut();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(MustAttackEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Phyrexian Juggernaut resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new PhyrexianJuggernaut()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phyrexian Juggernaut");
    }

    // ===== Must attack =====

    @Test
    @DisplayName("Declaring no attackers when Phyrexian Juggernaut can attack throws exception")
    void mustAttackWhenAble() {
        Permanent juggernaut = new Permanent(new PhyrexianJuggernaut());
        juggernaut.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(juggernaut);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Phyrexian Juggernaut does not need to attack with summoning sickness")
    void doesNotAttackWithSummoningSickness() {
        Permanent juggernaut = new Permanent(new PhyrexianJuggernaut());
        // summoning sick by default
        gd.playerBattlefields.get(player1.getId()).add(juggernaut);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Only Grizzly Bears can attack, Juggernaut has summoning sickness
        gs.declareAttackers(gd, player1, List.of(1));

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    // ===== Infect: combat damage deals poison to player =====

    @Test
    @DisplayName("Unblocked Phyrexian Juggernaut deals 5 poison counters instead of life loss")
    void dealsPoisonCountersWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent juggernaut = new Permanent(new PhyrexianJuggernaut());
        juggernaut.setSummoningSick(false);
        juggernaut.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(juggernaut);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Life should remain unchanged
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Poison counters should equal power (5)
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(5);
    }

    // ===== Infect: combat damage deals -1/-1 counters to creatures =====

    @Test
    @DisplayName("Blocked Phyrexian Juggernaut deals -1/-1 counters to blocker")
    void dealsMinusCountersToBlocker() {
        // Serra Angel is 4/4 with flying + vigilance — can block
        Permanent blockerPerm = new Permanent(new SerraAngel());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        Permanent juggernaut = new Permanent(new PhyrexianJuggernaut());
        juggernaut.setSummoningSick(false);
        juggernaut.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(juggernaut);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(juggernaut);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        harness.passBothPriorities();

        // Serra Angel (4/4) deals 4 damage to Juggernaut (5/5) — Juggernaut survives
        harness.assertOnBattlefield(player1, "Phyrexian Juggernaut");

        // Serra Angel should have 5 -1/-1 counters (from 5 infect damage), making it -1/-1 — dies
        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player2, "Serra Angel");

        // No poison counters — damage went to a creature
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }
}
