package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorpseChurn.class, Forest.class, GrizzlyBears.class, LightningBolt.class})
class CorpseChurnTest extends BaseCardTest {

    @Test
    @DisplayName("Mills three cards, then returns a chosen creature card to hand")
    void millsThenReturnsCreature() {
        Card firstMilled = new Forest();
        Card secondMilled = new Forest();
        Card creature = new GrizzlyBears();
        cast(List.of(firstMilled, secondMilled, creature));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, indexOfCard(creature));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(firstMilled, secondMilled)
                .doesNotContain(creature);
    }

    @Test
    @DisplayName("Declining the return leaves the milled cards in the graveyard")
    void decliningReturnLeavesMilledCardsInGraveyard() {
        Card firstMilled = new Forest();
        Card secondMilled = new Forest();
        Card thirdMilled = new Forest();
        cast(List.of(firstMilled, secondMilled, thirdMilled));

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(firstMilled, secondMilled, thirdMilled);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(firstMilled, secondMilled, thirdMilled);
    }

    @Test
    @DisplayName("Only creature cards are offered for the optional return")
    void onlyCreatureCardsCanBeReturned() {
        Card nonCreature = new LightningBolt();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonCreature, creature));
        cast(List.of(new Forest(), new Forest(), new Forest()));

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(indexOfCard(creature));
    }

    private void cast(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new CorpseChurn()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private int indexOfCard(Card card) {
        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        for (int i = 0; i < graveyard.size(); i++) {
            if (graveyard.get(i).getId().equals(card.getId())) {
                return i;
            }
        }
        throw new AssertionError("Card not found in graveyard: " + card.getId());
    }
}
