package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheCloningOfShredder.class, GrizzlyBears.class, HillGiant.class})
class TheCloningOfShredderTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I exiles a creature and creates a nonlegendary Mutant copy")
    void chapterIExilesCreatureAndCreatesMutantCopy() {
        Card creature = new GrizzlyBears();
        creature.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        harness.setGraveyard(player1, List.of(creature));
        Permanent saga = addSaga(0);

        advanceToNextChapter();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Grizzly Bears");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.MUTANT);
        assertThat(gd.getCardsExiledByPermanent(saga.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("Chapter II creates a copy of a creature exiled with the Saga")
    void chapterIICopiesCreatureExiledWithSaga() {
        Permanent saga = addSaga(1);
        Card creature = new GrizzlyBears();
        creature.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        gd.addToExile(player1.getId(), creature, saga.getId());

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Grizzly Bears");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.MUTANT);
    }

    @Test
    @DisplayName("Chapter II lets the controller choose among multiple creatures exiled with the Saga")
    void chapterIILetsControllerChooseExiledCreature() {
        Permanent saga = addSaga(1);
        Card first = new GrizzlyBears();
        Card second = new HillGiant();
        gd.addToExile(player1.getId(), first, saga.getId());
        gd.addToExile(player1.getId(), second, saga.getId());

        advanceToNextChapter();
        harness.passBothPriorities();

        PendingInteraction.ExiledCreatureCopyChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ExiledCreatureCopyChoice.class);
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId());
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(findPermanents(player1, "Hill Giant")).hasSize(1);
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheCloningOfShredder());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
