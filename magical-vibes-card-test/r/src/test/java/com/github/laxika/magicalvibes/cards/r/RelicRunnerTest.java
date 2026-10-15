package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.cards.k.KwendePrideOfFemeref;
import com.github.laxika.magicalvibes.cards.m.MerfolkTrickster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RelicRunner.class, GrizzlyBears.class, Ornithopter.class,
        KwendePrideOfFemeref.class, HistoryOfBenalia.class, MerfolkTrickster.class})
class RelicRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Relic Runner can be blocked when no historic spell was cast this turn")
    void canBeBlockedWhenNoHistoricSpellCast() {
        harness.setLife(player2, 20);

        // Defender has a creature that can block
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Relic Runner is attacking
        Permanent runner = harness.addToBattlefieldAndReturn(player1, new RelicRunner());
        runner.setSummoningSick(false);
        runner.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        // Blocking should succeed — no historic spell cast this turn
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Defender's life should remain 20 (Relic Runner was blocked)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Relic Runner can't be blocked after controller casts a historic spell (artifact)")
    void cantBeBlockedAfterCastingArtifact() {
        // Relic Runner is on the battlefield
        Permanent runner = harness.addToBattlefieldAndReturn(player1, new RelicRunner());
        runner.setSummoningSick(false);
        runner.setAttacking(true);

        // Defender has a creature
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Simulate that player1 cast a historic spell (artifact) this turn
        gd.recordSpellCast(player1.getId(), new Ornithopter());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

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
        Permanent runner = harness.addToBattlefieldAndReturn(player1, new RelicRunner());
        runner.setSummoningSick(false);

        // Give player1 an Ornithopter (artifact creature, historic) in hand and cast it
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();

        // Now player1 has cast a historic spell this turn
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isGreaterThan(0);

        // Defender has a creature
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Attack with Relic Runner
        runner.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

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
        Permanent runner = harness.addToBattlefieldAndReturn(player1, new RelicRunner());
        runner.setSummoningSick(false);
        runner.setAttacking(true);

        // Defender has a creature
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Player1 has cast a non-historic spell (spellsCastThisTurn > 0 but not historic)
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        // No historic spells in the list — GrizzlyBears is not an artifact, legendary, or Saga

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        // Blocking should succeed — only non-historic spells were cast
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Defender's life should remain 20 (Relic Runner was blocked)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Unblocked Relic Runner deals 2 damage")
    void dealsTwoDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent runner = harness.addToBattlefieldAndReturn(player1, new RelicRunner());
        runner.setSummoningSick(false);
        runner.setAttacking(true);

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

    @Test
    @DisplayName("A legendary nonartifact spell makes Relic Runner unblockable")
    void becomesUnblockableAfterCastingLegendaryCreature() {
        Permanent runner = harness.addToBattlefieldAndReturn(player1, new RelicRunner());
        runner.setSummoningSick(false);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new KwendePrideOfFemeref(), "{3}{W}");
        harness.passBothPriorities();

        assertCannotBlock(runner);
    }

    @Test
    @DisplayName("A nonlegendary Saga spell makes Relic Runner unblockable")
    void becomesUnblockableAfterCastingSaga() {
        Permanent runner = harness.addToBattlefieldAndReturn(player1, new RelicRunner());
        runner.setSummoningSick(false);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new HistoryOfBenalia(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertCannotBlock(runner);
    }

    @Test
    @DisplayName("An opponent's historic spell does not make Relic Runner unblockable")
    void opponentHistoricSpellDoesNotPreventBlocking() {
        Permanent runner = harness.addToBattlefieldAndReturn(player1, new RelicRunner());
        harness.addToBattlefield(player2, new GrizzlyBears());
        gd.recordSpellCast(player2.getId(), new Ornithopter());

        prepareBlockers(runner);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(runner.isBlockedThisCombat()).isTrue();
    }

    @Test
    @DisplayName("Putting an artifact onto the battlefield does not count as casting")
    void artifactEnteringWithoutCastingDoesNotPreventBlocking() {
        Permanent runner = harness.addToBattlefieldAndReturn(player1, new RelicRunner());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Ornithopter());

        prepareBlockers(runner);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(runner.isBlockedThisCombat()).isTrue();
    }

    @Test
    @DisplayName("A historic spell cast before Relic Runner enters still counts")
    void historicSpellCastBeforeRunnerEntersCounts() {
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        Permanent runner = harness.addToBattlefieldAndReturn(player1, new RelicRunner());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertCannotBlock(runner);
    }

    @Test
    @DisplayName("Removing Relic Runner's abilities allows the defender to block it")
    void losingAbilitiesRestoresBlockerDeclaration() {
        Permanent runner = harness.addToBattlefieldAndReturn(player1, new RelicRunner());
        runner.setSummoningSick(false);
        runner.setAttacking(true);
        harness.addToBattlefield(player2, new GrizzlyBears());
        gd.recordSpellCast(player1.getId(), new Ornithopter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new MerfolkTrickster()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castCreature(player2, 0, 0, runner.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(runner.isAttacking()).isTrue();
        assertThat(runner.isLosesAllAbilitiesUntilEndOfTurn()).isTrue();
        assertThat(harness.getCombatBlockService().getBlockableAttackerIndices(
                gd, player1.getId(), player2.getId())).contains(0);
    }

    private void prepareBlockers(Permanent runner) {
        runner.setSummoningSick(false);
        runner.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void assertCannotBlock(Permanent runner) {
        prepareBlockers(runner);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(runner);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }
}
