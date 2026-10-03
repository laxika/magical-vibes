package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CabalInterrogator.class, CabalConditioning.class})
class CabalInterrogatorTest extends BaseCardTest {

    private PendingInteraction.RevealCardsDiscardChoice activeChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
    }

    private Permanent readyInterrogator() {
        Permanent interrogator = addCreatureReady(player1, new CabalInterrogator());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return interrogator;
    }

    @Test
    @DisplayName("Reveals X cards and discards the controller's choice")
    void revealsXCardsAndDiscardsChosenCard() {
        harness.setHand(player2, List.of(new CabalInterrogator(), new CabalConditioning(), new CabalInterrogator()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        Permanent interrogator = readyInterrogator();

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealCardsDiscardChoice reveal = activeChoice();
        assertThat(reveal).isNotNull();
        assertThat(reveal.revealStage()).isTrue();
        assertThat(reveal.decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(reveal.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player1, 1);

        assertThat(interrogator.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Cabal Conditioning");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("When X exceeds the hand size, the whole hand is revealed and one card is discarded")
    void revealsEntireSmallerHand() {
        harness.setHand(player2, List.of(new CabalConditioning()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        Permanent interrogator = readyInterrogator();

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealCardsDiscardChoice choice = activeChoice();
        assertThat(choice).isNotNull();
        assertThat(choice.revealStage()).isFalse();
        assertThat(choice.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.remainingCount()).isEqualTo(1);
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        assertThat(interrogator.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Cabal Conditioning");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetController() {
        harness.setHand(player1, List.of(new CabalConditioning()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        Permanent interrogator = readyInterrogator();

        harness.activateAbility(player1, 0, 1, player1.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealCardsDiscardChoice choice = activeChoice();
        assertThat(choice).isNotNull();
        assertThat(choice.revealStage()).isFalse();
        assertThat(choice.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player1, 0);

        assertThat(interrogator.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Cabal Conditioning");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("X equal to zero reveals and discards nothing")
    void zeroXDoesNothing() {
        harness.setHand(player2, List.of(new CabalConditioning()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        readyInterrogator();

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(activeChoice()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target only a player and only during a main phase of its controller's turn")
    void enforcesTargetAndTimingRestrictions() {
        Permanent interrogator = addCreatureReady(player1, new CabalInterrogator());
        Permanent target = addCreatureReady(player2, new CabalInterrogator());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");

        harness.forceActivePlayer(player2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(interrogator.isTapped()).isFalse();
    }

    @Test
    void emptyHandResolvesWithoutAChoice() {
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Permanent interrogator = readyInterrogator();

        harness.activateAbility(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(interrogator.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void targetChoosesSubsetAndControllerChoosesOnlyFromThatSubset() {
        CabalConditioning hidden = new CabalConditioning();
        CabalInterrogator revealed = new CabalInterrogator();
        harness.setHand(player2, List.of(hidden, revealed));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        readyInterrogator();

        harness.activateAbility(player1, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        assertThat(activeChoice().decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(activeChoice().revealedCardIds()).containsExactly(revealed.getId());
        assertThat(activeChoice().validIndices()).containsExactly(0);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(hidden);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(revealed);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        Permanent interrogator = readyInterrogator();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(interrogator.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWhileAnotherAbilityIsOnTheStack() {
        readyInterrogator();
        Permanent second = addCreatureReady(player1, new CabalInterrogator());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, 0, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    void newlyEnteredInterrogatorCannotPayTheTapCost() {
        harness.addToBattlefield(player1, new CabalInterrogator());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
