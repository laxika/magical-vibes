package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({Gravegouger.class, GrizzlyBears.class})
class GravegougerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles up to two cards from one graveyard and leaves-the-battlefield returns them")
    void exiledCardsReturnToTheirOwnersGraveyardsWhenGravegougerLeaves() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(first, second));
        castGravegouger();

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);

        Permanent gravegouger = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, gravegouger));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        harness.assertInGraveyard(player1, "Gravegouger");
    }

    @Test
    @DisplayName("ETB can exile only one card when the single graveyard has one card")
    void etbCanExileOnlyOneAvailableCard() {
        Card onlyCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(onlyCard));
        castGravegouger();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(onlyCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(onlyCard);
    }

    @Test
    @DisplayName("ETB may choose no cards")
    void etbMayChooseNoCards() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(first, second));
        castGravegouger();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ETB cards must be chosen from a single graveyard")
    void etbCardsMustShareGraveyard() {
        Card ownCard = new GrizzlyBears();
        Card opponentCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        castGravegouger();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(ownCard.getId(), opponentCard.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");

        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB resolves harmlessly when both graveyards are empty")
    void emptyGraveyardsNeedNoTargets() {
        harness.castFromHand(player1, new Gravegouger(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gravegouger");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Leaving before ETB resolves leaves the selected cards exiled")
    void leavingBeforeEntryTriggerResolvesDoesNotReturnLaterExiledCards() {
        Card target = new Gravegouger();
        harness.setGraveyard(player2, List.of(target));
        castGravegouger();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToHand(gd, source));
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();

        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        harness.assertInHand(player1, "Gravegouger");
    }

    @Test
    @DisplayName("ETB still exiles a remaining legal target when the other target leaves its graveyard")
    void entryTriggerResolvesForRemainingLegalTarget() {
        Card removed = new Gravegouger();
        Card remaining = new Gravegouger();
        harness.setGraveyard(player2, List.of(removed, remaining));
        castGravegouger();
        harness.handleMultipleCardsChosen(player1, List.of(removed.getId(), remaining.getId()));
        harness.setGraveyard(player2, List.of(remaining));
        harness.setHand(player2, List.of(removed));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(removed);
    }

    @Test
    @DisplayName("Each Gravegouger returns only the cards exiled by its own entry trigger")
    void separateSourcesKeepTheirExiledCardsSeparate() {
        Card first = new Gravegouger();
        Card second = new Gravegouger();
        harness.setGraveyard(player2, List.of(first, second));
        castGravegouger();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        harness.passBothPriorities();
        Permanent firstSource = gd.playerBattlefields.get(player1.getId()).getFirst();

        castGravegouger();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        harness.passBothPriorities();
        Permanent secondSource = gd.playerBattlefields.get(player1.getId()).getLast();

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToHand(gd, firstSource));
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(second);

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToHand(gd, secondSource));
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private void castGravegouger() {
        harness.castFromHand(player1, new Gravegouger(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }
}
