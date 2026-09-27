package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.InteractionOptions;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CongregationAtDawn.class, BorosRecruit.class, Forest.class})
class CongregationAtDawnTest extends BaseCardTest {

    @Test
    @DisplayName("Offers only creature cards and at most three choices")
    void offersAtMostThreeCreatureCards() {
        Card creatureA = new BorosRecruit();
        Card creatureB = new BorosRecruit();
        Card creatureC = new BorosRecruit();
        Card creatureD = new BorosRecruit();
        harness.setLibrary(player1, List.of(creatureA, new Forest(), creatureB, creatureC, creatureD));

        cast();

        PendingInteraction.SearchLibraryToTopChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryToTopChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.pool()).containsExactlyInAnyOrder(creatureA, creatureB, creatureC, creatureD);
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(((InteractionOptions.MultiCardPick) choice.legalOptions()).maxCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Rejects a fourth creature and puts three chosen creatures on top in order")
    void limitsSelectionAndOrdersChosenCreatures() {
        Card creatureA = new BorosRecruit();
        Card creatureB = new BorosRecruit();
        Card creatureC = new BorosRecruit();
        Card creatureD = new BorosRecruit();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(creatureA, creatureB, creatureC, creatureD, forest));

        cast();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(creatureA.getId(), creatureB.getId(), creatureC.getId(), creatureD.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1,
                List.of(creatureA.getId(), creatureB.getId(), creatureC.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 3))
                .containsExactly(creatureC, creatureA, creatureB);
        assertThat(gd.playerDecks.get(player1.getId())).contains(forest, creatureD).hasSize(5);
        assertThat(gameLogContains("reveals")).isTrue();
    }

    @Test
    @DisplayName("Allows choosing zero creature cards")
    void allowsChoosingZeroCards() {
        Card creature = new BorosRecruit();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(creature, forest));

        cast();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, forest);
    }

    @Test
    @DisplayName("Allows choosing one creature card and puts it directly on top")
    void allowsChoosingOneCard() {
        Card creature = new BorosRecruit();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest, creature));

        cast();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature, forest);
    }

    @Test
    @DisplayName("Finishes without a choice when the library has no creature cards")
    void finishesWhenNoCreatureCardsAreFound() {
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        cast();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    private void cast() {
        harness.castFromHand(player1, new CongregationAtDawn(), "{G}{G}{W}");
        harness.passBothPriorities();
    }
}
