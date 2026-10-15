package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TravelThroughCaradhras.class, Forest.class, GrizzlyBears.class, TheValeyard.class})
class TravelThroughCaradhrasTest extends BaseCardTest {

    @Test
    @DisplayName("Each Redhorn Pass vote searches for a basic land tapped")
    void redhornPassVotesSearchBasicLands() {
        Forest firstLand = new Forest();
        Forest secondLand = new Forest();
        TravelThroughCaradhras spell = cast(List.of(firstLand, secondLand), List.of());

        harness.handleListChoice(player1, ChoiceContext.TravelThroughCaradhrasChoice.REDHORN_PASS);
        harness.handleListChoice(player2, ChoiceContext.TravelThroughCaradhrasChoice.REDHORN_PASS);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Forest")).allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Each Mines of Moria vote returns a card from the caster's graveyard")
    void minesOfMoriaVotesReturnCardsFromGraveyard() {
        Card firstCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        TravelThroughCaradhras spell = cast(List.of(), List.of(firstCard, secondCard));

        harness.handleListChoice(player1, ChoiceContext.TravelThroughCaradhrasChoice.MINES_OF_MORIA);
        harness.handleListChoice(player2, ChoiceContext.TravelThroughCaradhrasChoice.MINES_OF_MORIA);

        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard, secondCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Mixed votes apply both results only to the caster")
    void mixedVotesApplyBothResultsToCaster() {
        Forest land = new Forest();
        Card recovered = new GrizzlyBears();
        Card opponentsCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentsCard));
        TravelThroughCaradhras spell = cast(List.of(land), List.of(recovered));

        harness.handleListChoice(player1, ChoiceContext.TravelThroughCaradhrasChoice.MINES_OF_MORIA);
        harness.handleListChoice(player2, ChoiceContext.TravelThroughCaradhrasChoice.REDHORN_PASS);
        harness.handleCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Forest")).hasSize(1).allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(recovered);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Empty zones do not prevent the spell from being exiled")
    void emptyZonesStillExileSpell() {
        TravelThroughCaradhras spell = cast(List.of(), List.of());

        harness.handleListChoice(player1, ChoiceContext.TravelThroughCaradhrasChoice.REDHORN_PASS);
        harness.handleListChoice(player2, ChoiceContext.TravelThroughCaradhrasChoice.MINES_OF_MORIA);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The Valeyard grants the caster an additional vote before the opponent votes")
    void valeyardGrantsAdditionalVote() {
        harness.addToBattlefield(player1, new TheValeyard());
        cast(List.of(), List.of());

        harness.handleListChoice(player1, ChoiceContext.TravelThroughCaradhrasChoice.REDHORN_PASS);

        PendingInteraction.ColorChoice nextVote =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(nextVote).isNotNull();
        assertThat(nextVote.playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, ChoiceContext.TravelThroughCaradhrasChoice.MINES_OF_MORIA);
        harness.handleListChoice(player2, ChoiceContext.TravelThroughCaradhrasChoice.MINES_OF_MORIA);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private TravelThroughCaradhras cast(List<Card> library, List<Card> graveyard) {
        TravelThroughCaradhras spell = new TravelThroughCaradhras();
        harness.setLibrary(player1, library);
        harness.setGraveyard(player1, graveyard);
        harness.castFromHand(player1, spell, "{5}{G}");
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options())
                .containsExactlyElementsOf(ChoiceContext.TravelThroughCaradhrasChoice.OPTIONS);
        return spell;
    }
}
