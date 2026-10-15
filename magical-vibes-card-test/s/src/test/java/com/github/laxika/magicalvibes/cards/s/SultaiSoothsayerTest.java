package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SultaiSoothsayer.class, AlpineGrizzly.class, TormentingVoice.class, Forest.class, Island.class})
class SultaiSoothsayerTest extends BaseCardTest {

    @Test
    void etbPutsOneOfTopFourIntoHandAndTheRestIntoGraveyard() {
        Card topCard = new TormentingVoice();
        Card secondCard = new Forest();
        Card thirdCard = new Island();
        Card fourthCard = new AlpineGrizzly();
        Card belowTopFour = new TormentingVoice();
        harness.setLibrary(player1, List.of(topCard, secondCard, thirdCard, fourthCard, belowTopFour));

        harness.castFromHand(player1, new SultaiSoothsayer(), "{2}{B}{G}{U}");
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(topCard, secondCard, thirdCard, fourthCard);

        harness.handleMultipleCardsChosen(player1, List.of(thirdCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(thirdCard);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(topCard, secondCard, fourthCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowTopFour);
    }

    @Test
    void mustChooseOneCardWhenCardsAreAvailable() {
        Card first = new Forest();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.castFromHand(player1, new SultaiSoothsayer(), "{2}{B}{G}{U}");
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
    }

    @Test
    void looksAtAllAvailableCardsWhenLibraryHasFewerThanFour() {
        Card first = new Forest();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.castFromHand(player1, new SultaiSoothsayer(), "{2}{B}{G}{U}");
        resolveAllTriggers();

        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void putsTheOnlyAvailableCardIntoHand() {
        Card onlyCard = new Forest();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.castFromHand(player1, new SultaiSoothsayer(), "{2}{B}{G}{U}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryDoesNotRequireAChoice() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new SultaiSoothsayer(), "{2}{B}{G}{U}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Sultai Soothsayer");
    }
}
