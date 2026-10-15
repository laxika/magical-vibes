package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VedalkenOrrery;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrizzlyBears.class, RelentlessAssault.class, VedalkenOrrery.class})
class RelentlessAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts Relentless Assault on the stack as a sorcery with no target")
    void castingPutsItOnStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isNull();
    }

    @Test
    @DisplayName("Resolving untaps only creatures that attacked this turn")
    void resolvingUntapsOnlyAttackedCreatures() {
        Permanent attackedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttackedCreature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        nonAttackedCreature.tap();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();

        assertThat(attackedCreature.isTapped()).isFalse();
        assertThat(nonAttackedCreature.isTapped()).isTrue();
        assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);
    }

    @Test
    @CardUsed(VedalkenOrrery.class)
    @DisplayName("Resolving untaps creatures that attacked this turn regardless of controller")
    void resolvingUntapsAnOpponentsAttacker() {
        harness.addToBattlefield(player1, new VedalkenOrrery());
        Permanent opponentAttacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();

        assertThat(opponentAttacker.isTapped()).isFalse();
        assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);
    }

    @Test
    @DisplayName("Additional combat begins after postcombat main when Relentless Assault resolves")
    void additionalCombatBeginsAfterPostcombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Additional combat begins immediately after the precombat main phase")
    void additionalCombatBeginsAfterPrecombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.additionalCombatMainPhasePairs).isZero();
    }

    @Test
    @CardUsed(VedalkenOrrery.class)
    @DisplayName("Does not create extra phases when it resolves outside a main phase")
    void doesNotCreateExtraPhasesOutsideMainPhase() {
        harness.addToBattlefield(player1, new VedalkenOrrery());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Attacked-this-turn status resets on turn change")
    void attackedThisTurnResetsOnTurnChange() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        assertThat(creature.isAttackedThisTurn()).isTrue();

        harness.forceStep(TurnStep.CLEANUP);
        gs.advanceStep(gd);
        assertThat(creature.isAttackedThisTurn()).isFalse();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        creature.tap();

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Precombat resolution leaves the original combat after the added combat and main phase")
    void precombatResolutionPreservesOriginalCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }

    @Test
    @CardUsed(VedalkenOrrery.class)
    @DisplayName("Resolving outside a main phase only untaps attacked creatures")
    void resolvingOutsideMainPhaseOnlyUntapsAttackedCreatures() {
        harness.addToBattlefield(player1, new VedalkenOrrery());
        Permanent attackedCreature = addCreatureReady(player1, new GrizzlyBears());
        attackedCreature.setAttackedThisTurn(true);
        attackedCreature.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();

        assertThat(attackedCreature.isTapped()).isFalse();
        assertThat(gd.additionalCombatMainPhasePairs).isZero();
    }

    @Test
    @DisplayName("Two resolutions in one main phase create two combat and main phase pairs")
    void twoResolutionsCreateTwoPhasePairs() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();

        for (int combat = 0; combat < 2; combat++) {
            gs.advanceStep(gd);
            assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
            gs.advanceStep(gd);
            assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
            gs.advanceStep(gd);
            assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);
            gs.advanceStep(gd);
            assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        }

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }

    @Test
    @CardUsed(VedalkenOrrery.class)
    @DisplayName("Casting during an opponent's main phase gives that opponent the additional combat")
    void opponentGetsAdditionalCombatOnTheirTurn() {
        harness.addToBattlefield(player1, new VedalkenOrrery());
        Permanent opponentAttacker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(player2, List.of(0));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();

        assertThat(opponentAttacker.isTapped()).isFalse();
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());

        declareAttackers(player2, List.of(0));
        assertThat(opponentAttacker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A precombat cast leaves the normal combat after the additional main phase")
    void precombatCastLeavesNormalCombatAfterAdditionalMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(1);

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }
}
