package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DuneMover;
import com.github.laxika.magicalvibes.cards.a.AnointWithAffliction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TestamentBearer.class, DuneMover.class, AnointWithAffliction.class})
class TestamentBearerTest extends BaseCardTest {

    @Test
    @DisplayName("When Testament Bearer dies, one of the top three cards goes to hand and the rest go to the graveyard")
    void deathTriggerChoosesOneCardAndGraveyardsTheRest() {
        Permanent bearer = harness.addToBattlefieldAndReturn(player1, new TestamentBearer());
        Card chosen = new DuneMover();
        Card restOne = new AnointWithAffliction();
        Card restTwo = new AnointWithAffliction();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(chosen, restOne, restTwo));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bearer));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(bearer.getCard(), restOne, restTwo);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With one card in the library, that card goes to hand")
    void shortLibraryPutsAvailableCardsIntoHand() {
        Permanent bearer = harness.addToBattlefieldAndReturn(player1, new TestamentBearer());
        Card onlyCard = new DuneMover();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(onlyCard));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bearer));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bearer.getCard());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotDeclinePuttingOneCardIntoHand() {
        Permanent bearer = harness.addToBattlefieldAndReturn(player1, new TestamentBearer());
        Card first = new DuneMover();
        Card second = new AnointWithAffliction();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bearer));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void twoCardLibraryStillRequiresChoosingOnlyOne() {
        Permanent bearer = harness.addToBattlefieldAndReturn(player1, new TestamentBearer());
        Card chosen = new AnointWithAffliction();
        Card rest = new DuneMover();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(chosen, rest));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bearer));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(bearer.getCard(), rest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryDoesNotRequireAChoiceOrCauseDrawingLoss() {
        Permanent bearer = harness.addToBattlefieldAndReturn(player1, new TestamentBearer());
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bearer));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bearer.getCard());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
