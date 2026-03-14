package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BloodcrazedNeonateTest extends BaseCardTest {

    private Permanent addReadyNeonate() {
        Permanent perm = new Permanent(new BloodcrazedNeonate());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Has MustAttackEffect and PutCountersOnSourceEffect")
    void hasCorrectEffects() {
        BloodcrazedNeonate card = new BloodcrazedNeonate();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(MustAttackEffect.class);

        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst())
                .isInstanceOf(PutCountersOnSourceEffect.class);
    }

    // ===== Must attack =====

    @Test
    @DisplayName("Declaring no attackers when Bloodcrazed Neonate can attack throws exception")
    void mustAttackWhenAble() {
        addReadyNeonate();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Bloodcrazed Neonate does not need to attack with summoning sickness")
    void doesNotAttackWithSummoningSickness() {
        harness.setLife(player2, 20);

        Permanent neonate = new Permanent(new BloodcrazedNeonate());
        gd.playerBattlefields.get(player1.getId()).add(neonate);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Combat damage +1/+1 counter trigger =====

    @Test
    @DisplayName("Gets a +1/+1 counter when dealing combat damage to a player")
    void getsCounterOnCombatDamage() {
        Permanent neonate = addReadyNeonate();
        neonate.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // through combat damage

        // Player2 takes 2 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Neonate should have a +1/+1 counter
        assertThat(neonate.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals increased combat damage after getting a +1/+1 counter")
    void dealsMoreDamageWithCounter() {
        Permanent neonate = addReadyNeonate();
        neonate.setPlusOnePlusOneCounters(1); // simulate having gotten a counter previously
        neonate.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // 2 base power + 1 from counter = 3 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);

        // Resolve trigger — gets another counter
        harness.passBothPriorities();
        assertThat(neonate.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("No counter when blocked and killed")
    void noCounterWhenBlockedAndKilled() {
        Permanent neonate = addReadyNeonate();
        neonate.setAttacking(true);

        // 4/4 blocker kills the 2/1 Neonate
        Permanent blocker = new Permanent(new SerraAngel());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        // Neonate should be dead
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Bloodcrazed Neonate"));
    }
}
