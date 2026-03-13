package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BallLightningTest extends BaseCardTest {

    @Test
    @DisplayName("Ball Lightning has end-step sacrifice trigger")
    void hasEndStepSacrificeTrigger() {
        BallLightning card = new BallLightning();

        assertThat(card.getEffects(EffectSlot.END_STEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.END_STEP_TRIGGERED).getFirst())
                .isInstanceOf(SacrificeSelfEffect.class);
    }

    @Test
    @DisplayName("Can attack immediately due to haste and deals 6 damage")
    void canAttackImmediatelyAndDealsSixDamage() {
        harness.setLife(player2, 20);

        Permanent ballLightning = new Permanent(new BallLightning());
        ballLightning.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(ballLightning);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to defending player")
    void trampleAssignsExcessDamageToDefendingPlayer() {
        harness.setLife(player2, 20);

        Permanent ballLightning = new Permanent(new BallLightning());
        ballLightning.setSummoningSick(false);
        ballLightning.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(ballLightning);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // 6/1 trample blocked by 2/2 → assign lethal (2) to blocker, excess (4) to player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                bears.getId(), 2,
                player2.getId(), 4
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Ball Lightning"));
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Sacrifices itself at end step")
    void sacrificesItselfAtEndStep() {
        Permanent ballLightning = new Permanent(new BallLightning());
        gd.playerBattlefields.get(player1.getId()).add(ballLightning);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Ball Lightning");
        assertThat(trigger.getSourcePermanentId()).isEqualTo(ballLightning.getId());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Ball Lightning"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ball Lightning"));
    }

    @Test
    @DisplayName("Casting requires RRR mana")
    void castingRequiresTripleRed() {
        harness.setHand(player1, List.of(new BallLightning()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Ball Lightning");
    }
}
