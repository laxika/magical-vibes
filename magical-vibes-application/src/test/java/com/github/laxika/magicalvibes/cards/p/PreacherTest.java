package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Preacher.class, GrizzlyBears.class})
class PreacherTest extends BaseCardTest {

    @Test
    @DisplayName("The chosen opponent chooses a creature they control")
    void opponentChoosesCreatureTheyControl() {
        Permanent preacher = addCreatureReady(player1, new Preacher());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, player2.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validPermanentIds()).contains(opponentCreature.getId())
                .doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player2, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getId().equals(opponentCreature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(
                permanent -> permanent.getId().equals(opponentCreature.getId()));
        assertThat(preacher.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target its controller as the chosen opponent")
    void chosenPlayerMustBeAnOpponent() {
        addCreatureReady(player1, new Preacher());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability has no effect if Preacher untaps before resolution")
    void noEffectIfPreacherUntapsBeforeResolution() {
        Permanent preacher = addCreatureReady(player1, new Preacher());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player2, bears.getId());
        preacher.untap();
        preacher.tap();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(
                permanent -> permanent.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Control lasts while Preacher remains tapped")
    void controlEndsWhenPreacherUntaps() {
        Permanent preacher = addCreatureReady(player1, new Preacher());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player2, bears.getId());
        harness.passBothPriorities();

        advanceToNextTurn(player1);
        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(preacher.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(
                permanent -> permanent.getId().equals(bears.getId()));
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
