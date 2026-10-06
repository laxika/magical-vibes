package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CabarettiInitiate;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SizzlingSoloist.class, CabarettiInitiate.class})
class SizzlingSoloistTest extends BaseCardTest {

    @Test
    @DisplayName("Alliance targets only an opponent's creature and makes it unable to block")
    void allianceTargetsOpponentCreature() {
        harness.addToBattlefield(player1, new SizzlingSoloist());
        Permanent ownCreature = addCreatureReady(player1, new CabarettiInitiate());
        Permanent opponentCreature = addCreatureReady(player2, new CabarettiInitiate());
        prepareAllianceTrigger();

        triggerAlliance(opponentCreature.getId());

        assertThat(opponentCreature.isCantBlockThisTurn()).isTrue();
        assertThat(opponentCreature.isMustAttackThisCombat()).isFalse();
        assertThat(ownCreature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The second Alliance resolution makes its target attack during the next combat")
    void secondResolutionForcesNextCombatAttack() {
        harness.addToBattlefield(player1, new SizzlingSoloist());
        Permanent firstTarget = addCreatureReady(player2, new CabarettiInitiate());
        Permanent secondTarget = addCreatureReady(player2, new CabarettiInitiate());
        prepareAllianceTrigger();

        triggerAlliance(firstTarget.getId());
        triggerAlliance(secondTarget.getId());

        assertThat(secondTarget.isMustAttackThisCombat()).isFalse();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(secondTarget.isMustAttackThisCombat()).isTrue();
        assertThat(firstTarget.isMustAttackThisCombat()).isFalse();

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only the second resolution imposes an attack requirement")
    void thirdResolutionDoesNotForceAttack() {
        harness.addToBattlefield(player1, new SizzlingSoloist());
        Permanent firstTarget = addCreatureReady(player2, new CabarettiInitiate());
        Permanent secondTarget = addCreatureReady(player2, new CabarettiInitiate());
        Permanent thirdTarget = addCreatureReady(player2, new CabarettiInitiate());
        prepareAllianceTrigger();

        triggerAlliance(firstTarget.getId());
        triggerAlliance(secondTarget.getId());
        triggerAlliance(thirdTarget.getId());

        assertThat(thirdTarget.isCantBlockThisTurn()).isTrue();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(firstTarget.isMustAttackThisCombat()).isFalse();
        assertThat(secondTarget.isMustAttackThisCombat()).isTrue();
        assertThat(thirdTarget.isMustAttackThisCombat()).isFalse();
    }

    @Test
    @DisplayName("Soloist does not trigger for itself or an opponent's creature entering")
    void ignoresSelfAndOpponentEntries() {
        Permanent opponentCreature = addCreatureReady(player2, new CabarettiInitiate());
        prepareAllianceTrigger();

        harness.castFromHand(player1, new SizzlingSoloist(), "{3}{R}");
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(opponentCreature.isCantBlockThisTurn()).isFalse();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new CabarettiInitiate(), "{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(opponentCreature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("An ability with an illegal target does not count as a resolution")
    void illegalTargetDoesNotCountTowardsSecondResolution() {
        harness.addToBattlefield(player1, new SizzlingSoloist());
        Permanent removedTarget = addCreatureReady(player2, new CabarettiInitiate());
        Permanent firstTarget = addCreatureReady(player2, new CabarettiInitiate());
        Permanent secondTarget = addCreatureReady(player2, new CabarettiInitiate());
        prepareAllianceTrigger();

        chooseAllianceTarget(removedTarget.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, removedTarget));
        harness.passBothPriorities();
        triggerAlliance(firstTarget.getId());
        triggerAlliance(secondTarget.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(firstTarget.isMustAttackThisCombat()).isFalse();
        assertThat(secondTarget.isMustAttackThisCombat()).isTrue();
    }

    @Test
    @DisplayName("The second resolution still forces an attack after Soloist leaves")
    void secondResolutionWorksAfterSourceLeaves() {
        Permanent soloist = harness.addToBattlefieldAndReturn(player1, new SizzlingSoloist());
        Permanent target = addCreatureReady(player2, new CabarettiInitiate());
        prepareAllianceTrigger();

        triggerAlliance(target.getId());
        chooseAllianceTarget(target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, soloist));
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(target.isMustAttackThisCombat()).isTrue();
    }

    @Test
    @DisplayName("The attack requirement survives cleanup until the opponent's combat")
    void attackRequirementSurvivesTurnBoundary() {
        harness.addToBattlefield(player1, new SizzlingSoloist());
        Permanent target = addCreatureReady(player2, new CabarettiInitiate());
        harness.setLibrary(player2, List.of(new CabarettiInitiate()));
        prepareAllianceTrigger();

        triggerAlliance(target.getId());
        triggerAlliance(target.getId());
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(target.isMustAttackThisCombat()).isFalse();
        assertThat(target.isCantBlockThisTurn()).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(target.isCantBlockThisTurn()).isFalse();
        assertThat(target.isMustAttackThisCombat()).isTrue();
        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareAllianceTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void triggerAlliance(UUID targetId) {
        chooseAllianceTarget(targetId);
        harness.passBothPriorities();
    }

    private void chooseAllianceTarget(UUID targetId) {
        harness.castFromHand(player1, new CabarettiInitiate(), "{G}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(targetId)
                .doesNotContainAnyElementsOf(gd.playerBattlefields.get(player1.getId()).stream()
                        .map(Permanent::getId).toList());

        harness.handlePermanentChosen(player1, targetId);
    }
}
