package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfernalTutor.class, Forest.class})
class InfernalTutorTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals a card from hand and searches for a card with the same name")
    void searchesForSameNameAsCardInHand() {
        harness.setHand(player1, List.of(new InfernalTutor(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        addManaForInfernalTutor();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice handChoice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(handChoice.options()).containsExactly("Forest");

        harness.handleListChoice(player1, "Forest");

        PendingInteraction.LibrarySearch search =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Forest");
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest", "Forest");
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.deferPlayerLossCheck).isFalse();
    }

    @Test
    @DisplayName("Searches for any card when the controller has no cards in hand")
    void searchesForAnyCardWithEmptyHand() {
        harness.setHand(player1, List.of(new InfernalTutor()));
        harness.setLibrary(player1, List.of(new Forest()));
        addManaForInfernalTutor();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Forest");
        assertThat(search.params().reveals()).isFalse();
        assertThat(search.params().canFailToFind()).isFalse();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.deferPlayerLossCheck).isFalse();
    }

    private void addManaForInfernalTutor() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
