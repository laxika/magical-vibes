package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrizzlyBears.class, Ornithopter.class, VisionQuest.class})
class VisionQuestTest extends BaseCardTest {

    @Test
    @DisplayName("Searches the graveyard and puts the artifact creature onto the battlefield without haste at X=0")
    void searchesGraveyardWithoutHasteAtZero() {
        Card ornithopter = new Ornithopter();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(ornithopter));
        castVisionQuest(0);

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.pool()).containsExactly(ornithopter);

        harness.handleMultipleCardsChosen(player1, List.of(ornithopter.getId()));

        Permanent found = findPermanent(player1, "Ornithopter");
        assertThat(found.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(found.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Searches the library, adds X counters, and grants haste when X is at least four")
    void searchesLibraryWithCountersAndHasteAtFour() {
        Card ornithopter = new Ornithopter();
        harness.setLibrary(player1, List.of(ornithopter, new GrizzlyBears()));
        harness.setGraveyard(player1, List.of());
        castVisionQuest(4);

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.pool()).containsExactly(ornithopter);

        harness.handleMultipleCardsChosen(player1, List.of(ornithopter.getId()));

        Permanent found = findPermanent(player1, "Ornithopter");
        assertThat(found.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(found.hasKeyword(Keyword.HASTE)).isTrue();
    }

    private void castVisionQuest(int xValue) {
        harness.setHand(player1, List.of(new VisionQuest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castSorcery(player1, 0, xValue);
        harness.passBothPriorities();
    }
}
