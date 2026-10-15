package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.y.YavimayaCradleOfGrowth;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MentalJourney.class, Forest.class, Island.class, Plains.class,
        YavimayaCradleOfGrowth.class})
class MentalJourneyTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving draws three cards")
    void drawsThreeCards() {
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new MentalJourney()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 3);
    }

    @Test
    @DisplayName("Basic landcycling discards the card and offers only basic lands")
    void basicLandcyclingSearchesBasicLands() {
        harness.setHand(player1, List.of(new MentalJourney()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mental Journey");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC))
                .hasSize(3);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new MentalJourney()));
    }

    @Test
    void basicLandcyclingPutsChosenLandIntoHandWithoutDrawing() {
        Plains land = new Plains();
        MentalJourney card = new MentalJourney();
        MentalJourney otherCard = new MentalJourney();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(land, otherCard, new YavimayaCradleOfGrowth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(land);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2).contains(otherCard).doesNotContain(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void basicLandcyclingCanFailToFindEvenWithBasicLandAvailable() {
        Plains land = new Plains();
        harness.setHand(player1, List.of(new MentalJourney()));
        harness.setLibrary(player1, List.of(land));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        harness.assertInGraveyard(player1, "Mental Journey");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void basicLandcyclingWithNoBasicLandsDoesNotDraw() {
        MentalJourney otherCard = new MentalJourney();
        YavimayaCradleOfGrowth nonbasicLand = new YavimayaCradleOfGrowth();
        harness.setHand(player1, List.of(new MentalJourney()));
        harness.setLibrary(player1, List.of(otherCard, nonbasicLand));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(otherCard, nonbasicLand);
        harness.assertInGraveyard(player1, "Mental Journey");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void basicLandcyclingRequiresBlueManaBeforeDiscarding() {
        MentalJourney card = new MentalJourney();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
        assertThat(gd.stack).isEmpty();
    }
}
