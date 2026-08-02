package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AmbushViper;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GiftsUngivenTest extends BaseCardTest {

    private void castGiftsUngiven(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new GiftsUngiven()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private void pickFromLibrary(String name) {
        List<String> offered = offeredNames();
        int index = offered.indexOf(name);
        assertThat(index).isGreaterThanOrEqualTo(0);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }

    private List<String> offeredNames() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
    }

    private PendingInteraction.MultiGraveyardChoice opponentChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
    }

    @Test
    @DisplayName("Opponent chooses two of the four revealed cards for the graveyard; the rest go to hand")
    void opponentChoosesTwoForGraveyard() {
        Card island = new Island();
        Card shock = new Shock();
        Card divination = new Divination();
        Card viper = new AmbushViper();
        castGiftsUngiven(List.of(island, shock, divination, viper));

        pickFromLibrary("Island");
        pickFromLibrary("Shock");
        pickFromLibrary("Divination");
        pickFromLibrary("Ambush Viper");

        PendingInteraction.MultiGraveyardChoice choice = opponentChoice();
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).hasSize(4);
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player2, List.of(shock.getId(), viper.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock, viper);
        assertThat(gd.playerHands.get(player1.getId())).contains(island, divination);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
        harness.assertInGraveyard(player1, "Gifts Ungiven");
    }

    @Test
    @DisplayName("Each pick must have a different name")
    void picksMustHaveDifferentNames() {
        castGiftsUngiven(List.of(new Island(), new Island(), new Shock()));

        pickFromLibrary("Island");

        assertThat(offeredNames()).containsExactly("Shock");
    }

    @Test
    @DisplayName("Stopping the search early still hands the smaller pool to the opponent")
    void stoppingEarlyStillOffersTheFoundCards() {
        Card island = new Island();
        Card shock = new Shock();
        Card divination = new Divination();
        castGiftsUngiven(List.of(island, shock, divination));

        pickFromLibrary("Island");
        pickFromLibrary("Shock");
        pickFromLibrary("Divination");

        harness.handleMultipleCardsChosen(player2, List.of(island.getId(), shock.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island, shock);
        assertThat(gd.playerHands.get(player1.getId())).contains(divination);
    }

    @Test
    @DisplayName("Declining a later pick hands the cards found so far to the opponent")
    void decliningALaterPickKeepsTheFoundCards() {
        Card island = new Island();
        Card shock = new Shock();
        castGiftsUngiven(List.of(island, shock, new Divination(), new AmbushViper()));

        pickFromLibrary("Island");
        pickFromLibrary("Shock");
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        PendingInteraction.MultiGraveyardChoice choice = opponentChoice();
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(island.getId(), shock.getId());

        harness.handleMultipleCardsChosen(player2, List.of(island.getId(), shock.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island, shock);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Finding a single card lets the opponent send only that one to the graveyard")
    void singleFoundCardIsTheOnlyChoice() {
        Card island = new Island();
        castGiftsUngiven(List.of(island));

        pickFromLibrary("Island");

        PendingInteraction.MultiGraveyardChoice choice = opponentChoice();
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player2, List.of(island.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(island);
    }

    @Test
    @DisplayName("Declining the first pick finds nothing and the spell simply finishes")
    void decliningTheSearchFindsNothing() {
        castGiftsUngiven(List.of(new Island(), new Shock()));

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Gifts Ungiven");
    }

    @Test
    @DisplayName("The opponent must choose the full two cards")
    void opponentCannotChooseFewerThanTwo() {
        Card island = new Island();
        castGiftsUngiven(List.of(island, new Shock(), new Divination(), new AmbushViper()));

        pickFromLibrary("Island");
        pickFromLibrary("Shock");
        pickFromLibrary("Divination");
        pickFromLibrary("Ambush Viper");

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opponentChoice()).isNotNull();
    }

    @Test
    @DisplayName("Gifts Ungiven cannot target its own caster")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new GiftsUngiven()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
