package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfControllerCastHistoricSpellThisTurnEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RelicRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Relic Runner has correct static effect")
    void hasCorrectEffect() {
        RelicRunner card = new RelicRunner();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(CantBeBlockedIfControllerCastHistoricSpellThisTurnEffect.class);
    }

    @Test
    @DisplayName("Relic Runner can be blocked when no historic spell was cast this turn")
    void canBeBlockedWhenNoHistoricSpellCast() {
        harness.setLife(player2, 20);

        // Defender has a creature that can block
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        // Relic Runner is attacking
        Permanent runner = new Permanent(new RelicRunner());
        runner.setSummoningSick(false);
        runner.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(runner);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        // Blocking should succeed — no historic spell cast this turn
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Defender's life should remain 20 (Relic Runner was blocked)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Relic Runner can't be blocked after controller casts a historic spell (artifact)")
    void cantBeBlockedAfterCastingArtifact() {
        // Relic Runner is on the battlefield
        Permanent runner = new Permanent(new RelicRunner());
        runner.setSummoningSick(false);
        runner.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(runner);

        // Defender has a creature
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        // Simulate that player1 cast a historic spell (artifact) this turn
        gd.recordSpellCast(player1.getId(), new Ornithopter());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        // Attempting to block should fail — GrizzlyBears is at blocker index 0, RelicRunner is at attacker index 0
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Relic Runner becomes unblockable after actually casting an artifact creature")
    void becomesUnblockableAfterCastingArtifactCreature() {
        harness.setLife(player2, 20);

        // Put Relic Runner on battlefield (not summoning sick)
        Permanent runner = new Permanent(new RelicRunner());
        runner.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(runner);

        // Give player1 an Ornithopter (artifact creature, historic) in hand and cast it
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Now player1 has cast a historic spell this turn
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isGreaterThan(0);

        // Defender has a creature
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        // Attack with Relic Runner
        runner.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        // Attempting to block should fail — artifact was cast this turn
        // GrizzlyBears is at blocker index 0, RelicRunner is at attacker index 0
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Relic Runner can be blocked when only non-historic spells were cast")
    void canBeBlockedWhenOnlyNonHistoricSpellCast() {
        harness.setLife(player2, 20);

        // Relic Runner is on the battlefield
        Permanent runner = new Permanent(new RelicRunner());
        runner.setSummoningSick(false);
        runner.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(runner);

        // Defender has a creature
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        // Player1 has cast a non-historic spell (spellsCastThisTurn > 0 but not historic)
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        // No historic spells in the list — GrizzlyBears is not an artifact, legendary, or Saga

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        // Blocking should succeed — only non-historic spells were cast
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Defender's life should remain 20 (Relic Runner was blocked)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Unblocked Relic Runner deals 2 damage")
    void dealsTwoDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent runner = new Permanent(new RelicRunner());
        runner.setSummoningSick(false);
        runner.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(runner);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Historic tracking is cleared at turn start")
    void historicTrackingClearedAtTurnStart() {
        // Simulate that player1 cast a historic spell
        gd.recordSpellCast(player1.getId(), new Ornithopter());
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isGreaterThan(0);

        // Advance to next turn
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Historic tracking should be cleared (spellsCastThisTurn is cleared at turn start)
        assertThat(gd.isSpellsCastThisTurnEmpty()).isTrue();
    }
}
