package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TravelThroughCaradhras.class, Forest.class, GrizzlyBears.class})
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

    private TravelThroughCaradhras cast(List<Card> library, List<Card> graveyard) {
        TravelThroughCaradhras spell = new TravelThroughCaradhras();
        harness.setLibrary(player1, library);
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options())
                .containsExactlyElementsOf(ChoiceContext.TravelThroughCaradhrasChoice.OPTIONS);
        return spell;
    }
}
