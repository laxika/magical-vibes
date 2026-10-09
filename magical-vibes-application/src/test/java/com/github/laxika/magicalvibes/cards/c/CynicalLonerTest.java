package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EnduringInnocence;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CynicalLoner.class, EnduringInnocence.class})
class CynicalLonerTest extends BaseCardTest {

    @Test
    void acceptedSurvivalSearchPutsCardIntoGraveyard() {
        Permanent loner = harness.addToBattlefieldAndReturn(player1, new CynicalLoner());
        loner.tap();
        harness.setLibrary(player1, List.of(new CynicalLoner()));

        advanceToPostcombatMain();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Cynical Loner");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void declinedSurvivalSearchLeavesLibraryUnchanged() {
        Permanent loner = harness.addToBattlefieldAndReturn(player1, new CynicalLoner());
        loner.tap();
        harness.setLibrary(player1, List.of(new CynicalLoner()));

        advanceToPostcombatMain();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertNotInGraveyard(player1, "Cynical Loner");
    }

    @Test
    void untappedLonerDoesNotTriggerSurvival() {
        harness.addToBattlefieldAndReturn(player1, new CynicalLoner());
        harness.setLibrary(player1, List.of(new CynicalLoner()));

        advanceToPostcombatMain();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotBeBlockedByGlimmer() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new EnduringInnocence());
        blocker.setSummoningSick(false);

        Permanent loner = harness.addToBattlefieldAndReturn(player1, new CynicalLoner());
        loner.setSummoningSick(false);
        loner.setAttacking(true);

        prepareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(loner);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeBlockedByNonGlimmer() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new CynicalLoner());
        blocker.setSummoningSick(false);

        Permanent loner = harness.addToBattlefieldAndReturn(player1, new CynicalLoner());
        loner.setSummoningSick(false);
        loner.setAttacking(true);

        prepareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(loner);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void untappingBeforeResolutionStopsSurvivalSearch() {
        Permanent loner = harness.addToBattlefieldAndReturn(player1, new CynicalLoner());
        loner.tap();
        harness.setLibrary(player1, List.of(new CynicalLoner()));

        advanceToPostcombatMain();
        assertThat(gd.stack).hasSize(1);
        loner.untap();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertNotInGraveyard(player1, "Cynical Loner");
    }

    @Test
    void survivalUsesTappedStatusWhenSourceLeavesBattlefield() {
        Permanent loner = harness.addToBattlefieldAndReturn(player1, new CynicalLoner());
        loner.tap();
        harness.setLibrary(player1, List.of(new EnduringInnocence()));

        advanceToPostcombatMain();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, loner);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Enduring Innocence");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void acceptedSurvivalWithEmptyLibraryFinishesWithoutChoosingCard() {
        Permanent loner = harness.addToBattlefieldAndReturn(player1, new CynicalLoner());
        loner.tap();
        harness.setLibrary(player1, List.of());

        advanceToPostcombatMain();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void survivalDoesNotTriggerDuringOpponentsSecondMainPhase() {
        Permanent loner = harness.addToBattlefieldAndReturn(player2, new CynicalLoner());
        loner.tap();

        advanceToPostcombatMain();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void survivalDoesNotTriggerDuringThirdMainPhase() {
        Permanent loner = harness.addToBattlefieldAndReturn(player1, new CynicalLoner());
        loner.tap();

        advanceToPostcombatMain();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        gd.additionalCombatMainPhasePairs = 1;
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, harness::passBothPriorities);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void advanceToPostcombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    private void prepareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
