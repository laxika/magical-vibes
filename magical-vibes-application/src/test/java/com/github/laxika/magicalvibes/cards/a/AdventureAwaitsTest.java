package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdventureAwaits.class, Forest.class, GrizzlyBears.class, Shock.class})
class AdventureAwaitsTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a revealed creature into hand and the rest on the bottom")
    void revealsCreatureIntoHand() {
        Card creature = new GrizzlyBears();
        Card firstNoncreature = new Shock();
        Card secondNoncreature = new Forest();
        Card thirdNoncreature = new Shock();
        Card fourthNoncreature = new Forest();
        List<Card> topCards = List.of(creature, firstNoncreature, secondNoncreature, thirdNoncreature,
                fourthNoncreature);
        castAdventureAwaits(topCards);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstNoncreature, secondNoncreature, thirdNoncreature,
                        fourthNoncreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Draws a card when the top five contain no creature")
    void drawsWhenNoCreatureIsFound() {
        Card firstNoncreature = new Shock();
        Card secondNoncreature = new Forest();
        Card thirdNoncreature = new Shock();
        Card fourthNoncreature = new Forest();
        Card fifthNoncreature = new Shock();
        Card drawnCard = new GrizzlyBears();
        List<Card> topCards = List.of(firstNoncreature, secondNoncreature, thirdNoncreature,
                fourthNoncreature, fifthNoncreature);
        castAdventureAwaits(List.of(firstNoncreature, secondNoncreature, thirdNoncreature,
                fourthNoncreature, fifthNoncreature, drawnCard));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(topCards);
    }

    private void castAdventureAwaits(List<Card> topCards) {
        harness.forceActivePlayer(player1);
        harness.setLibrary(player1, topCards);
        harness.setHand(player1, List.of(new AdventureAwaits()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
    }
}
