package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SailIntoTheWest.class, GrizzlyBears.class})
class SailIntoTheWestTest extends BaseCardTest {

    @Test
    @DisplayName("A return majority lets each player return up to two graveyard cards and exiles the spell")
    void returnMajorityReturnsCardsAndExilesSpell() {
        SailIntoTheWest spell = new SailIntoTheWest();
        Card player1First = new GrizzlyBears();
        Card player1Second = new GrizzlyBears();
        Card player2First = new GrizzlyBears();
        Card player2Second = new GrizzlyBears();
        harness.setHand(player1, List.of(spell));
        harness.setGraveyard(player1, List.of(player1First, player1Second));
        harness.setGraveyard(player2, List.of(player2First, player2Second));

        cast(spell);
        harness.handleListChoice(player1, ChoiceContext.SailIntoTheWestChoice.RETURN);
        harness.handleListChoice(player2, ChoiceContext.SailIntoTheWestChoice.RETURN);

        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player2, 0);
        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(player1First, player1Second);
        assertThat(gd.playerHands.get(player2.getId())).contains(player2First, player2Second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A tied vote resolves as embark and keeps the spell in the graveyard")
    void tiedVoteLetsPlayersDiscardAndDrawIndependently() {
        SailIntoTheWest spell = new SailIntoTheWest();
        Card player1HandCard = new GrizzlyBears();
        Card player2HandCard = new GrizzlyBears();
        harness.setHand(player1, List.of(spell, player1HandCard));
        harness.setHand(player2, List.of(player2HandCard));
        harness.setLibrary(player1, sevenCards());
        harness.setLibrary(player2, sevenCards());

        cast(spell);
        harness.handleListChoice(player1, ChoiceContext.SailIntoTheWestChoice.RETURN);
        harness.handleListChoice(player2, ChoiceContext.SailIntoTheWestChoice.EMBARK);

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(player2HandCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(player1HandCard, spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
    }

    private void cast(SailIntoTheWest spell) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyElementsOf(ChoiceContext.SailIntoTheWestChoice.OPTIONS);
    }

    private List<Card> sevenCards() {
        return List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
    }
}
