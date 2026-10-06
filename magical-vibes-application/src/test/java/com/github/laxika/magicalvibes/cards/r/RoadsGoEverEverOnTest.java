package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.IdyllicGrange;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoadsGoEverEverOn.class, Plains.class, IdyllicGrange.class, Forest.class,
        GrizzlyBears.class})
class RoadsGoEverEverOnTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I exiles up to two basic Plains and gains 2 life")
    void chapterIExilesBasicPlainsAndGainsLife() {
        Card plains1 = new Plains();
        Card nonbasicPlains = new IdyllicGrange();
        Card forest = new Forest();
        Card plains2 = new Plains();
        harness.setLibrary(player1, List.of(plains1, nonbasicPlains, forest, plains2));
        Permanent saga = addSagaWithLore(0);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToChapter();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(plains1, plains2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getCardsExiledByPermanent(saga.getId()))
                .containsExactlyInAnyOrder(plains1, plains2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(nonbasicPlains, forest);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Chapters II and III return a card exiled with the Saga to its owner's hand")
    void chaptersIIAndIIIReturnExiledCardsToHand() {
        Permanent saga = addSagaWithLore(1);
        Card chapterII = new Plains();
        gd.addToExile(player1.getId(), chapterII, saga.getId());

        advanceToChapter();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Plains");
        assertThat(gd.getCardsExiledByPermanent(saga.getId())).isEmpty();

        Card chapterIII = new Plains();
        gd.addToExile(player1.getId(), chapterIII, saga.getId());
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToChapter();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(chapterII, chapterIII);
        assertThat(gd.getCardsExiledByPermanent(saga.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chapter IV targets a creature you control and scales with your Plains")
    void chapterIVTargetsOwnCreatureAndCountsPlains() {
        Permanent saga = addSagaWithLore(3);
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        advanceToChapter();
        harness.passBothPriorities();
        declareAttackers(player1, List.of(3));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(attacker.getId(), target.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    @Test
    void chapterICanFindNoCardsAndStillGainLife() {
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(plains));
        Permanent saga = addSagaWithLore(0);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToChapter();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getCardsExiledByPermanent(saga.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void chapterIStopsAfterTwoEvenWhenMorePlainsAreAvailable() {
        Card first = new Plains();
        Card second = new Plains();
        Card third = new Plains();
        harness.setLibrary(player1, List.of(first, second, third));
        Permanent saga = addSagaWithLore(0);

        advanceToChapter();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getCardsExiledByPermanent(saga.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterIIChoosesExactlyOneOfTwoExiledCards() {
        Permanent saga = addSagaWithLore(1);
        Card first = new Plains();
        Card second = new Plains();
        gd.addToExile(player1.getId(), first, saga.getId());
        gd.addToExile(player1.getId(), second, saga.getId());

        advanceToChapter();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(second).doesNotContain(first);
        assertThat(gd.getCardsExiledByPermanent(saga.getId())).containsExactly(first);
    }

    @Test
    void chapterIIReturnsCardToOwnerAfterControlChanges() {
        assertChapterReturnsToOwnerAfterControlChanges(1);
    }

    @Test
    void chapterIIIReturnsCardToOwnerAfterControlChanges() {
        assertChapterReturnsToOwnerAfterControlChanges(2);
    }

    private void assertChapterReturnsToOwnerAfterControlChanges(int loreCounters) {
        Permanent saga = addSagaWithLore(loreCounters);
        Card plains = new Plains();
        gd.addToExile(player1.getId(), plains, saga.getId());
        gd.playerBattlefields.get(player1.getId()).remove(saga);
        gd.playerBattlefields.get(player2.getId()).add(saga);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(plains);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(plains);
        assertThat(gd.getCardsExiledByPermanent(saga.getId())).isEmpty();
    }

    @Test
    void chapterIVTriggersOnceForMultipleAttackersAndCountsPlainsAtResolution() {
        addSagaWithLore(3);
        harness.addToBattlefield(player1, new Plains());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());

        advanceToChapter();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Roads Go Ever, Ever On");
        declareAttackers(player1, List.of(1, 2));
        harness.handlePermanentChosen(player1, first.getId());
        harness.addToBattlefield(player1, new Plains());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new RoadsGoEverEverOn());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
