package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CoilingStalker;
import com.github.laxika.magicalvibes.cards.h.HandOfEnlightenment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EraOfEnlightenment.class, HandOfEnlightenment.class, CoilingStalker.class})
class EraOfEnlightenmentTest extends BaseCardTest {

    @Test
    void chapterIScriesTwo() {
        Card first = new CoilingStalker();
        Card second = new CoilingStalker();
        Card third = new CoilingStalker();
        harness.setLibrary(player1, List.of(first, second, third));
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(first, second);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
    }

    @Test
    void chapterIIGainsTwoLife() {
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    void chapterIIIExilesAndReturnsTheSagaTransformedUnderItsController() {
        addSagaWithLore(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Hand of Enlightenment");
        assertThat(returned.isTransformed()).isTrue();
    }

    @Test
    void castingSagaImmediatelyScriesAndAllowsKeepingBothCardsOnTop() {
        Card first = new CoilingStalker();
        Card second = new CoilingStalker();
        harness.setLibrary(player1, List.of(first, second));

        harness.castFromHand(player1, new EraOfEnlightenment(), "{1}{W}");
        resolveAllTriggers();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(findPermanent(player1, "Era of Enlightenment").getCounterCount(CounterType.LORE))
                .isEqualTo(1);
    }

    @Test
    void returnedCreatureCannotAttackTheTurnItEnters() {
        Permanent saga = addSagaWithLore(2);
        advanceToNextChapter();
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Hand of Enlightenment");
        assertThat(returned.getId()).isNotEqualTo(saga.getId());
        assertThat(returned.getCounterCount(CounterType.LORE)).isZero();
        assertThat(als.canAttack(gd, returned, player1.getId())).isFalse();
    }

    @Test
    void chapterIIIReturnsUnderTriggerControllersControlDespiteLaterControlChange() {
        Permanent saga = addSagaWithLore(2);
        advanceToNextChapter();
        assertThat(gd.stack).isNotEmpty();

        gd.playerBattlefields.get(player1.getId()).remove(saga);
        gd.playerBattlefields.get(player2.getId()).add(saga);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Hand of Enlightenment");
        harness.assertNotOnBattlefield(player2, "Hand of Enlightenment");
    }

    @Test
    void chapterIIIDoesNotReturnSagaThatAlreadyLeftBattlefield() {
        Permanent saga = addSagaWithLore(2);
        advanceToNextChapter();
        gd.playerBattlefields.get(player1.getId()).remove(saga);
        harness.setGraveyard(player1, List.of(saga.getOriginalCard()));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Era of Enlightenment");
        harness.assertNotOnBattlefield(player1, "Hand of Enlightenment");
    }

    @Test
    void backFaceKillsBlockerBeforeItCanDealRegularCombatDamage() {
        addSagaWithLore(2);
        advanceToNextChapter();
        resolveAllTriggers();
        Permanent returned = findPermanent(player1, "Hand of Enlightenment");
        harness.performUntapStep(player1);
        addCreatureReady(player2, new CoilingStalker());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Coiling Stalker");
        harness.assertOnBattlefield(player1, "Hand of Enlightenment");
        assertThat(returned.getMarkedDamage()).isZero();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new EraOfEnlightenment());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
