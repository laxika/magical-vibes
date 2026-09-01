package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.f.FireLordSozin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheRiseOfSozin.class, FireLordSozin.class, ChildOfNight.class,
        GrizzlyBears.class, HillGiant.class})
class TheRiseOfSozinTest extends BaseCardTest {

    @Test
    void chapterIDestroysAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new ChildOfNight());
        Permanent saga = addSaga(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Child of Night");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    @Test
    void chapterIIExilesUpToFourNamedCardsFromTargetOpponentsZones() {
        Card graveyardCard = new GrizzlyBears();
        Card handCard = new GrizzlyBears();
        Card libraryCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setHand(player2, List.of(handCard));
        harness.setLibrary(player2, List.of(libraryCard));
        addSaga(1);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Grizzly Bears");
        harness.handleMultipleCardsChosen(player1,
                List.of(graveyardCard.getId(), handCard.getId(), libraryCard.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(3);
    }

    @Test
    void chapterIIITransformsTheSaga() {
        addSaga(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent sozin = findPermanent(player1, "Fire Lord Sozin");
        assertThat(sozin).isNotNull();
        assertThat(sozin.isTransformed()).isTrue();
        harness.assertNotOnBattlefield(player1, "The Rise of Sozin");
    }

    @Test
    void fireLordSozinReturnsTargetCreaturesFromDamagedPlayersGraveyardWithinPaidX() {
        addTransformedSaga();
        Card bears = new GrizzlyBears();
        Card child = new ChildOfNight();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player2, List.of(bears, child, tooExpensive));
        gd.playerManaPools.get(player1.getId()).addPersistentMana(ManaColor.COLORLESS, 4);

        declareAttackers(player1, List.of(0));
        harness.handleXValueChosen(player1, 4);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(
                bears.getId(), child.getId(), tooExpensive.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), child.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Child of Night");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Child of Night");
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheRiseOfSozin());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private Permanent addTransformedSaga() {
        TheRiseOfSozin front = new TheRiseOfSozin();
        Permanent saga = new Permanent(front);
        saga.setCard(front.getBackFaceCard());
        saga.setTransformed(true);
        saga.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(saga);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
