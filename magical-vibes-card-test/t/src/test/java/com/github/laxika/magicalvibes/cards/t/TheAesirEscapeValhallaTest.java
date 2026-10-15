package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheAesirEscapeValhalla.class, GrizzlyBears.class, Shock.class, Naturalize.class})
class TheAesirEscapeValhallaTest extends BaseCardTest {

    @Test
    @DisplayName("Chapters I and II use the exiled permanent's mana value")
    void chaptersUseExiledPermanentManaValue() {
        GrizzlyBears exiledPermanent = new GrizzlyBears();
        Shock instant = new Shock();
        harness.setGraveyard(player1, List.of(exiledPermanent, instant));
        harness.setLife(player1, 20);
        addSagaWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToNextChapter();

        PendingInteraction.MultiGraveyardChoice graveyardChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(graveyardChoice.validCardIds()).containsExactly(exiledPermanent.getId());
        harness.handleMultipleCardsChosen(player1, List.of(exiledPermanent.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiledPermanent);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter III returns the Saga and its exiled card to hand")
    void chapterIIIReturnsSagaAndExiledCardToHand() {
        GrizzlyBears exiledPermanent = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(exiledPermanent));
        Permanent saga = addSagaWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of(exiledPermanent.getId()));
        harness.passBothPriorities();

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(saga.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(saga.getCard(), exiledPermanent);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Entering the battlefield exiles a card and gains life in the same resolution")
    void enteringGainsLifeWithoutAnotherPriorityRound() {
        GrizzlyBears exiledPermanent = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(exiledPermanent));
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new TheAesirEscapeValhalla(), "{2}{G}");
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(exiledPermanent.getId()));

        harness.assertLife(player1, 22);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiledPermanent);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Repeating chapter I gains life only for the card exiled by that resolution")
    void repeatedChapterIGainsLifeOnlyForNewCard() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLife(player1, 20);
        Permanent saga = addSagaWithLore(0);

        advanceToNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        resolveAllTriggers();
        harness.assertLife(player1, 22);

        saga.setCounterCount(CounterType.LORE, 0);
        advanceToNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        resolveAllTriggers();

        harness.assertLife(player1, 24);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Chapters II and III use every card exiled by repeated chapter I resolutions")
    void laterChaptersUseAllLinkedCards() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        Permanent saga = addSagaWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        resolveAllTriggers();
        saga.setCounterCount(CounterType.LORE, 0);
        advanceToNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        resolveAllTriggers();

        advanceToNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        advanceToNextChapter();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).contains(saga.getCard(), first, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chapter I can exile the Saga itself after it is destroyed in response")
    void chapterICanExileItsOwnCardFromGraveyard() {
        harness.setLife(player1, 20);
        Permanent saga = addSagaWithLore(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, saga.getId());
        harness.assertInGraveyard(player1, "The Aesir Escape Valhalla");
        resolveAllTriggers();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(saga.getCard().getId());
        harness.handleMultipleCardsChosen(player1, List.of(saga.getCard().getId()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(saga.getCard());
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("An empty graveyard gives no life or counters and does not prevent returning the Saga")
    void emptyGraveyardStillAllowsLaterChapters() {
        harness.setLife(player1, 20);
        Permanent saga = addSagaWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToNextChapter();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        advanceToNextChapter();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).contains(saga.getCard());
        harness.assertNotOnBattlefield(player1, "The Aesir Escape Valhalla");
    }

    @Test
    @DisplayName("Chapter III returns the exiled card even if the Saga is destroyed in response")
    void chapterIIIReturnsExiledCardWithoutSaga() {
        GrizzlyBears exiledPermanent = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(exiledPermanent));
        Permanent saga = addSagaWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of(exiledPermanent.getId()));
        resolveAllTriggers();
        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, saga.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(exiledPermanent).doesNotContain(saga.getCard());
        harness.assertInGraveyard(player1, "The Aesir Escape Valhalla");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheAesirEscapeValhalla());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();
    }
}
