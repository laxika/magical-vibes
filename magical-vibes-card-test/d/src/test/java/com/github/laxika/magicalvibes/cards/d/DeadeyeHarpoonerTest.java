package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AegisAutomaton;
import com.github.laxika.magicalvibes.cards.b.BastionInventor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeadeyeHarpooner.class, AegisAutomaton.class, BastionInventor.class})
class DeadeyeHarpoonerTest extends BaseCardTest {

    @Test
    @DisplayName("Revolt ETB destroys a tapped creature an opponent controls")
    void revoltDestroysTappedOpponentCreature() {
        Permanent ownAutomaton = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, ownAutomaton));
        Permanent opponentAutomaton = harness.addToBattlefieldAndReturn(player2, new AegisAutomaton());
        opponentAutomaton.tap();

        castDeadeyeHarpooner();
        harness.handlePermanentChosen(player1, opponentAutomaton.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Aegis Automaton");
    }

    @Test
    @DisplayName("Without Revolt, the ETB ability does not trigger")
    void doesNotTriggerWithoutRevolt() {
        Permanent opponentAutomaton = harness.addToBattlefieldAndReturn(player2, new AegisAutomaton());
        opponentAutomaton.tap();

        castDeadeyeHarpooner();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Aegis Automaton");
    }

    @Test
    @DisplayName("The Revolt trigger only offers tapped opposing creatures")
    void triggerOnlyOffersTappedOpposingCreatures() {
        Permanent ownAutomaton = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        ownAutomaton.tap();
        Permanent opponentUntappedAutomaton = harness.addToBattlefieldAndReturn(player2, new AegisAutomaton());
        Permanent opponentTappedAutomaton = harness.addToBattlefieldAndReturn(player2, new AegisAutomaton());
        opponentTappedAutomaton.tap();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, ownAutomaton));

        castDeadeyeHarpooner();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(opponentTappedAutomaton.getId());
        assertThat(choice.validIds()).doesNotContain(opponentUntappedAutomaton.getId());
    }

    @Test
    @DisplayName("A permanent leaving under an opponent's control does not enable Revolt")
    void opponentPermanentLeavingDoesNotEnableRevolt() {
        Permanent opponentAutomaton = harness.addToBattlefieldAndReturn(player2, new AegisAutomaton());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, opponentAutomaton));
        Permanent remainingOpponentAutomaton = harness.addToBattlefieldAndReturn(player2, new AegisAutomaton());
        remainingOpponentAutomaton.tap();

        castDeadeyeHarpooner();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Aegis Automaton");
    }

    @Test
    @DisplayName("A target that untaps before resolution is not destroyed")
    void untappedTargetSurvivesResolution() {
        Permanent ownAutomaton = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, ownAutomaton));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AegisAutomaton());
        target.tap();

        castDeadeyeHarpooner();
        harness.handlePermanentChosen(player1, target.getId());
        target.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Aegis Automaton");
        harness.assertNotInGraveyard(player2, "Aegis Automaton");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapped opposing creatures with hexproof cannot be chosen")
    void hexproofCreatureIsExcludedFromTargets() {
        Permanent ownAutomaton = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, ownAutomaton));
        Permanent protectedTarget = harness.addToBattlefieldAndReturn(player2, new BastionInventor());
        protectedTarget.tap();
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player2, new AegisAutomaton());
        legalTarget.tap();

        castDeadeyeHarpooner();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(legalTarget.getId());
        harness.handlePermanentChosen(player1, legalTarget.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Bastion Inventor");
        harness.assertInGraveyard(player2, "Aegis Automaton");
    }

    @Test
    @DisplayName("Revolt with no legal target still lets the creature enter")
    void revoltWithoutLegalTargetDoesNotPreventEntering() {
        Permanent ownAutomaton = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, ownAutomaton));
        harness.addToBattlefield(player2, new AegisAutomaton());
        Permanent ownTappedCreature = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        ownTappedCreature.tap();

        castDeadeyeHarpooner();

        harness.assertOnBattlefield(player1, "Deadeye Harpooner");
        harness.assertOnBattlefield(player2, "Aegis Automaton");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castDeadeyeHarpooner() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DeadeyeHarpooner(), "{2}{W}");
        harness.passBothPriorities();
    }
}
