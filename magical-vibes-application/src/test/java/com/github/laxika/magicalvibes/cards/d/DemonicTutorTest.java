package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonicTutor.class, GrizzlyBears.class, Plains.class, Swamp.class})
class DemonicTutorTest extends BaseCardTest {

    @Test
    void searchesLibraryForAnyCardAndPutsItIntoHand() {
        Card tutor = new DemonicTutor();
        Card chosenCard = new GrizzlyBears();
        harness.setHand(player1, List.of(tutor));
        harness.addMana(player1, ManaColor.BLACK, 2);
        setLibrary(new Plains(), chosenCard, new Swamp());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyInAnyOrderElementsOf(
                gd.playerDecks.get(player1.getId()));
        assertThat(search.params().canFailToFind()).isFalse();
        assertThat(search.params().reveals()).isFalse();

        int chosenIndex = search.params().cards().indexOf(chosenCard);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(chosenIndex));

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(chosenCard.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(chosenCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(tutor.getId()));
    }

    @Test
    void unrestrictedSearchCannotFailToFind() {
        harness.setHand(player1, List.of(new DemonicTutor()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        setLibrary(new Plains());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThatThrownBy(() -> harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(-1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot fail to find");
    }

    private void setLibrary(Card... cards) {
        List<Card> library = harness.getGameData().playerDecks.get(player1.getId());
        library.clear();
        library.addAll(List.of(cards));
    }
}
