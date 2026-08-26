package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.TheGrandEvolution;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({VorinclexTheGrandEvolution.class, TheGrandEvolution.class,
        Forest.class, GrizzlyBears.class, HillGiant.class})
class VorinclexTheGrandEvolutionTest extends BaseCardTest {

    @Test
    @DisplayName("When Vorinclex enters, it searches for up to two Forests")
    void searchesForForests() {
        harness.setHand(player1, List.of(new VorinclexTheGrandEvolution()));
        harness.setLibrary(player1, List.of(
                new Forest(), new GrizzlyBears(), new Forest(), new HillGiant()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().cards()).allMatch(card -> card instanceof Forest);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).filteredOn(card -> card instanceof Forest).hasSize(2);
    }

    @Test
    @DisplayName("Chapter I offers only creature cards milled by this chapter and returns the chosen cards")
    void chapterIMillsAndReturnsChosenCreatures() {
        Permanent saga = addBackFaceSaga(0);
        Card first = new GrizzlyBears();
        Card second = new HillGiant();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(
                first, new Forest(), second, new Forest(), third,
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        advanceSagaToNextChapter();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                first.getId(), second.getId(), third.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(first.getId(), second.getId())
                .doesNotContain(third.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(third.getId());
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter III returns the Saga to its front face")
    void chapterIIIReturnsFrontFace() {
        addBackFaceSaga(2);

        advanceSagaToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> !permanent.isTransformed()
                        && permanent.getOriginalCard() instanceof VorinclexTheGrandEvolution);
    }

    private Permanent addBackFaceSaga(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new VorinclexTheGrandEvolution());
        saga.setCard(saga.getOriginalCard().getBackFaceCard());
        saga.setTransformed(true);
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void advanceSagaToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
