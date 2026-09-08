package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({SizzlingSoloist.class, GrizzlyBears.class})
class SizzlingSoloistTest extends BaseCardTest {

    @Test
    @DisplayName("Alliance targets only an opponent's creature and makes it unable to block")
    void allianceTargetsOpponentCreature() {
        harness.addToBattlefield(player1, new SizzlingSoloist());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
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
        Permanent firstTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());
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

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareAllianceTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void triggerAlliance(UUID targetId) {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(targetId);

        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
    }
}
