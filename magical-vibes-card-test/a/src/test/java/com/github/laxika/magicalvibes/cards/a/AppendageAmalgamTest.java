package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AppendageAmalgam.class})
class AppendageAmalgamTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking surveils 1")
    void attackingSurveilsOne() {
        addCreatureReady(player1, new AppendageAmalgam());
        Card topCard = new AppendageAmalgam();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        PendingInteraction.MayAbilityChoice surveil =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(surveil).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Appendage Amalgam");
    }

    @Test
    @DisplayName("Declining the attack surveil leaves the top card on the library")
    void decliningAttackSurveilLeavesTopCardOnLibrary() {
        addCreatureReady(player1, new AppendageAmalgam());
        Card topCard = new AppendageAmalgam();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertNotInGraveyard(player1, "Appendage Amalgam");
    }

    @Test
    @DisplayName("Flash permits casting during the opponent end step")
    void canCastDuringOpponentEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.castFromHand(player1, new AppendageAmalgam(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Appendage Amalgam");
    }

    @Test
    @DisplayName("An attack with an empty library completes without a choice")
    void attackingWithEmptyLibraryCompletes() {
        addCreatureReady(player1, new AppendageAmalgam());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playersWhoSurveilledThisTurn).contains(player1.getId());
    }

    @Test
    @DisplayName("An opponent controlled Amalgam surveils its controller library only")
    void opponentAttackSurveilsOpponentLibrary() {
        addCreatureReady(player2, new AppendageAmalgam());
        Card ownTop = new AppendageAmalgam();
        Card opponentTop = new AppendageAmalgam();
        harness.setLibrary(player1, List.of(ownTop));
        harness.setLibrary(player2, List.of(opponentTop));

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentTop);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTop);
        harness.assertNotInGraveyard(player1, "Appendage Amalgam");
    }
}
