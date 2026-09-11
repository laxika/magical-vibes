package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiaochanArtfulBeauty.class, ForestBear.class})
class DiaochanArtfulBeautyTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent chooses the second target while the ability is activated")
    void opponentChoosesSecondTargetWhileActivating() {
        Permanent diaochan = setupDiaochanOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent ownBear = addCreatureReady(player1, new ForestBear());
        Permanent firstTarget = addCreatureReady(player2, new ForestBear());

        harness.activateAbility(player1, 0, null, firstTarget.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ActivatedAbilityOpponentTarget.class);
        assertThat(choice.validPermanentIds()).contains(firstTarget.getId(), ownBear.getId(), diaochan.getId());

        harness.handlePermanentChosen(player2, ownBear.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Forest Bear");
        harness.assertInGraveyard(player1, "Forest Bear");
        harness.assertOnBattlefield(player1, "Diaochan, Artful Beauty");
        assertThat(diaochan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller may choose their own creature as the first target")
    void controllerMayChooseOwnCreatureAsFirstTarget() {
        Permanent diaochan = setupDiaochanOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent firstTarget = addCreatureReady(player1, new ForestBear());
        Permanent secondTarget = addCreatureReady(player2, new ForestBear());

        harness.activateAbility(player1, 0, null, firstTarget.getId());
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validPermanentIds()).contains(secondTarget.getId());

        harness.handlePermanentChosen(player2, secondTarget.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest Bear");
        harness.assertInGraveyard(player2, "Forest Bear");
        harness.assertOnBattlefield(player1, "Diaochan, Artful Beauty");
        assertThat(diaochan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent may choose Diaochan as the second target")
    void opponentMayChooseDiaochanAsSecondTarget() {
        Permanent diaochan = setupDiaochanOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent firstTarget = addCreatureReady(player2, new ForestBear());

        harness.activateAbility(player1, 0, null, firstTarget.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player2, diaochan.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Forest Bear");
        harness.assertNotOnBattlefield(player1, "Diaochan, Artful Beauty");
        harness.assertInGraveyard(player1, "Diaochan, Artful Beauty");
    }

    @Test
    @DisplayName("Opponent may choose the first target again")
    void opponentMayChooseFirstTargetAgain() {
        Permanent diaochan = setupDiaochanOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent firstTarget = addCreatureReady(player2, new ForestBear());

        harness.activateAbility(player1, 0, null, firstTarget.getId());
        harness.handlePermanentChosen(player2, firstTarget.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Forest Bear");
        harness.assertOnBattlefield(player1, "Diaochan, Artful Beauty");
        assertThat(diaochan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupDiaochanOnMyTurn(TurnStep.DECLARE_ATTACKERS);
        Permanent target = addCreatureReady(player2, new ForestBear());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during a later combat phase")
    void cannotActivateDuringLaterCombatPhase() {
        setupDiaochanOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        gd.combatPhasesThisTurn = 2;
        Permanent target = addCreatureReady(player2, new ForestBear());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        setupDiaochanOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player2);
        Permanent target = addCreatureReady(player2, new ForestBear());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    private Permanent setupDiaochanOnMyTurn(TurnStep step) {
        Permanent diaochan = addCreatureReady(player1, new DiaochanArtfulBeauty());
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
        return diaochan;
    }
}
