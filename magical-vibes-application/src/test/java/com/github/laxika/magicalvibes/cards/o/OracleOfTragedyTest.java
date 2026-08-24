package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OracleOfTragedy.class, Forest.class, GrizzlyBears.class, HillGiant.class, WrathOfGod.class})
class OracleOfTragedyTest extends BaseCardTest {

    @Test
    @DisplayName("The ETB draw mode draws then discards a card")
    void etbDrawsThenDiscards() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new OracleOfTragedy(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("The ETB shuffle mode targets only qualifying cards and allows up to four")
    void etbShufflesUpToFourQualifyingCards() {
        List<Card> qualifyingCards = List.of(
                new HillGiant(), new HillGiant(), new HillGiant(), new HillGiant(), new HillGiant());
        Card lowManaCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(lowManaCard, qualifyingCards.get(0), qualifyingCards.get(1),
                qualifyingCards.get(2), qualifyingCards.get(3), qualifyingCards.get(4)));
        harness.setHand(player1, List.of(new OracleOfTragedy()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyElementsOf(qualifyingCards.stream().map(Card::getId).toList());
        assertThat(choice.maxCount()).isEqualTo(4);

        harness.handleMultipleCardsChosen(player1, qualifyingCards.subList(0, 4).stream().map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(lowManaCard, qualifyingCards.get(4));
    }

    @Test
    @DisplayName("The death shuffle mode chooses graveyard cards when the trigger resolves")
    void deathShuffleModeTargetsGraveyardCardsAtResolution() {
        Card qualifyingCard = new HillGiant();
        Card lowManaCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new OracleOfTragedy());
        harness.setGraveyard(player1, List.of(qualifyingCard, lowManaCard));
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Shuffle up to four target cards with mana value 3 or greater from your graveyard into your library");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(qualifyingCard.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(lowManaCard.getId())
                .doesNotContain(qualifyingCard.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId).contains(qualifyingCard.getId());
    }
}
