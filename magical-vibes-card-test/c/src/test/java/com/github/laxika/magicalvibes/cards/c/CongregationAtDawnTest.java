package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenMindcensor;
import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.o.ObNixilisUnshackled;
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

@CardUsed({CongregationAtDawn.class, BorosRecruit.class, Forest.class, ObNixilisUnshackled.class,
        AvenMindcensor.class})
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

    @Test
    @DisplayName("Allows choosing two creatures and ordering them above the shuffled remainder")
    void allowsChoosingTwoCards() {
        Card creatureA = new BorosRecruit();
        Card creatureB = new BorosRecruit();
        Card creatureC = new BorosRecruit();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(creatureA, forest, creatureB, creatureC));

        cast();
        harness.handleMultipleCardsChosen(player1, List.of(creatureA.getId(), creatureB.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2))
                .containsExactly(creatureB, creatureA);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 4))
                .containsExactlyInAnyOrder(creatureC, forest);
        assertThat(gameLogContains("reveals")).isTrue();
    }

    @Test
    @DisplayName("Rejects noncreature and duplicate selections without losing the valid choice")
    void rejectsInvalidSelections() {
        Card creature = new BorosRecruit();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(creature, forest));

        cast();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature, forest);
    }

    @Test
    @DisplayName("Resolves with an empty library without prompting for cards")
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        cast();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof CongregationAtDawn);
    }
    @Test
    @DisplayName("The search is recorded, so an opponent's Ob Nixilis triggers")
    void searchIsRecorded() {
        harness.addToBattlefield(player2, new ObNixilisUnshackled());
        harness.setLibrary(player1, List.of(new Forest()));

        cast();
        resolveAllTriggers();

        assertThat(gd.playersWhoSearchedLibraryThisTurn).contains(player1.getId());
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("An opponent's Aven Mindcensor limits the search to the top four cards")
    void avenMindcensorLimitsTheSearch() {
        harness.addToBattlefield(player2, new AvenMindcensor());
        Card deep = new BorosRecruit();
        Card top = new BorosRecruit();
        harness.setLibrary(player1, List.of(top, new Forest(), new Forest(), new Forest(), deep));

        cast();

        PendingInteraction.SearchLibraryToTopChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryToTopChoice.class);
        assertThat(choice.pool()).containsExactly(top);
    }

    private void cast() {
        harness.castFromHand(player1, new CongregationAtDawn(), "{G}{G}{W}");
        harness.passBothPriorities();
    }
}
