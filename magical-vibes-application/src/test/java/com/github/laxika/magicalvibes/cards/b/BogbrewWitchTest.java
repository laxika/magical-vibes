package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BogbrewWitchTest extends BaseCardTest {

    private void setUpWitch() {
        harness.addToBattlefield(player1, new BogbrewWitch());
        findPermanent(player1, "Bogbrew Witch").setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }

    private Card namedCard(String name, CardType type) {
        Card card = new Card() {};
        card.setName(name);
        card.setType(type);
        return card;
    }

    @Test
    @DisplayName("Search offers only Festering Newt and Bubbling Cauldron")
    void searchOffersOnlyTheTwoNamedCards() {
        setUpWitch();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                namedCard("Festering Newt", CardType.CREATURE),
                namedCard("Bubbling Cauldron", CardType.ARTIFACT),
                new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Festering Newt", "Bubbling Cauldron");
    }

    @Test
    @DisplayName("Chosen card enters the battlefield tapped")
    void chosenCardEntersTapped() {
        setUpWitch();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                namedCard("Bubbling Cauldron", CardType.ARTIFACT),
                new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Bubbling Cauldron");
        Permanent cauldron = findPermanent(player1, "Bubbling Cauldron");
        assertThat(cauldron.isTapped()).isTrue();
    }
}
