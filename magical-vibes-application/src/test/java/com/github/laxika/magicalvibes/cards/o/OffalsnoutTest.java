package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.m.MorselTheft;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({Offalsnout.class, MorselTheft.class})
class OffalsnoutTest extends BaseCardTest {

    @Test
    @DisplayName("Flash allows Offalsnout to be hardcast during an opponent's turn")
    void canBeCastDuringOpponentsTurnBecauseOfFlash() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Offalsnout()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passPriority(player2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Evoke: sacrificed on entry, LTB exiles a target card from an opponent's graveyard")
    void evokeExilesOpponentGraveyardCard() {
        Card card = new MorselTheft();
        harness.setGraveyard(player2, List.of(card));
        harness.setHand(player1, List.of(new Offalsnout()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithEvoke(player1, 0, null);
        resolveAllTriggers(); // resolve creature spell and evoke sacrifice -> graveyard prompt

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        resolveAllTriggers(); // resolve LTB trigger -> exile

        harness.assertNotInGraveyard(player2, "Morsel Theft");
        assertThat(gd.exiledCards.stream().anyMatch(e -> e.card().getId().equals(card.getId()))).isTrue();
        // Offalsnout itself was sacrificed as it entered.
        harness.assertNotOnBattlefield(player1, "Offalsnout");
    }

    @Test
    @DisplayName("LTB fires on any leave and can exile a card from the controller's own graveyard")
    void leaveExilesOwnGraveyardCard() {
        Card card = new MorselTheft();
        harness.setGraveyard(player1, List.of(card));
        Permanent offalsnout = harness.addToBattlefieldAndReturn(player1, new Offalsnout());

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, offalsnout));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // drain LTB trigger -> graveyard prompt

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities(); // resolve LTB trigger -> exile

        harness.assertNotInGraveyard(player1, "Morsel Theft");
        assertThat(gd.exiledCards.stream().anyMatch(e -> e.card().getId().equals(card.getId()))).isTrue();
    }

    @Test
    @DisplayName("LTB target is mandatory when a graveyard card is available")
    void leaveRequiresTargetWhenCardIsAvailable() {
        Card card = new MorselTheft();
        harness.setGraveyard(player1, List.of(card));
        Permanent offalsnout = harness.addToBattlefieldAndReturn(player1, new Offalsnout());

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, offalsnout));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
    }
}
