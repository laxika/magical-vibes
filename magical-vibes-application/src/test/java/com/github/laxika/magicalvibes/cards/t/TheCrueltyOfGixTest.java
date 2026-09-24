package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheCrueltyOfGix.class, Forest.class, GrizzlyBears.class})
class TheCrueltyOfGixTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I reveals an opponent's hand and discards a chosen creature")
    void chapterIChoosesCreatureToDiscard() {
        Forest land = new Forest();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setHand(player2, List.of(land, creature));
        addSagaWithLore(0);

        triggerNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("Chapter II searches for a card and makes you lose 3 life")
    void chapterIISearchesAndLosesLife() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), creature));
        harness.setLife(player1, 20);
        addSagaWithLore(1);

        triggerNextChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(1));

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Chapter III puts a target creature card from a graveyard onto the battlefield")
    void chapterIIIReturnsCreatureUnderYourControl() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        Permanent saga = addSagaWithLore(2);

        triggerNextChapter();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheCrueltyOfGix());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
