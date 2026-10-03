package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FesteringNewt;
import com.github.laxika.magicalvibes.cards.p.PredatorySliver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BogbrewWitch.class, FesteringNewt.class, BubblingCauldron.class, PredatorySliver.class})
class BogbrewWitchTest extends BaseCardTest {

    private void setUpWitch() {
        harness.addToBattlefield(player1, new BogbrewWitch());
        findPermanent(player1, "Bogbrew Witch").setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }

    @Test
    @DisplayName("Search offers only Festering Newt and Bubbling Cauldron")
    void searchOffersOnlyTheTwoNamedCards() {
        setUpWitch();
        harness.setLibrary(player1, List.of(
                new FesteringNewt(),
                new BubblingCauldron(),
                new PredatorySliver()));

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
        harness.setLibrary(player1, List.of(
                new BubblingCauldron(),
                new PredatorySliver()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Bubbling Cauldron");
        Permanent cauldron = findPermanent(player1, "Bubbling Cauldron");
        assertThat(cauldron.isTapped()).isTrue();
    }

    @Test
    void newtEntersTappedAndOnlyOneCardIsFound() {
        setUpWitch();
        FesteringNewt newt = new FesteringNewt();
        BubblingCauldron cauldron = new BubblingCauldron();
        harness.setLibrary(player1, List.of(newt, cauldron));
        harness.setLibrary(player2, List.of(new BubblingCauldron()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(findPermanent(player1, "Bogbrew Witch").isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Festering Newt").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(cauldron);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayFailToFindEvenWithAMatchingCard() {
        setUpWitch();
        FesteringNewt newt = new FesteringNewt();
        harness.setLibrary(player1, List.of(newt));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(newt);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void noMatchingCardCompletesWithoutAChoice() {
        setUpWitch();
        PredatorySliver sliver = new PredatorySliver();
        harness.setLibrary(player1, List.of(sliver));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sliver);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
