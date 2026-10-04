package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Opt;
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

@CardUsed({ElspethsNightmare.class, Forest.class, GrizzlyBears.class, HillGiant.class, Opt.class})
class ElspethsNightmareTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I destroys only an opponent's creature with power 2 or less")
    void chapterIDestroysEligibleOpponentCreature() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        addSagaWithLore(0);

        triggerNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(opponentBears.getId());
        assertThat(choice.validIds()).doesNotContain(ownBears.getId(), opponentGiant.getId());

        harness.handlePermanentChosen(player1, opponentBears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownBears);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentGiant).doesNotContain(opponentBears);
    }

    @Test
    @DisplayName("Chapter II lets you choose a noncreature, nonland card for an opponent to discard")
    void chapterIITargetsOpponentHand() {
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        Card chosenCard = new Opt();
        harness.setHand(player2, List.of(land, creature, chosenCard));
        addSagaWithLore(1);

        triggerNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(2);
        harness.handleCardChosen(player1, 2);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(chosenCard);
    }

    @Test
    @DisplayName("Chapter III exiles the targeted opponent's graveyard")
    void chapterIIIExilesOpponentGraveyard() {
        Card graveyardCard = new GrizzlyBears();
        Permanent saga = addSagaWithLore(2);
        harness.setGraveyard(player2, List.of(graveyardCard));

        triggerNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(graveyardCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @Test
    @DisplayName("Entering the battlefield triggers chapter I immediately")
    void enteringTriggersChapterI() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        Permanent saga = harness.enterBattlefieldAndReturn(player1, new ElspethsNightmare());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter I does not destroy a target whose power becomes greater than two")
    void chapterIRechecksPowerOnResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSagaWithLore(0);
        triggerNextChapter();
        harness.handlePermanentChosen(player1, creature.getId());

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chapter II discards nothing when the hand contains only creatures and lands")
    void chapterIIWithNoEligibleCards() {
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setHand(player2, List.of(land, creature));
        addSagaWithLore(1);

        triggerNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)).isNull();
    }

    @Test
    @DisplayName("Chapter II chooses exactly one card when multiple noncreature, nonland cards are available")
    void chapterIIChoosesOneOfMultipleEligibleCards() {
        Card first = new ElspethsNightmare();
        Card second = new ElspethsNightmare();
        harness.setHand(player2, List.of(first, second));
        addSagaWithLore(1);

        triggerNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0, 1);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(second);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)).isNull();
    }

    @Test
    @DisplayName("Chapter III can target an empty graveyard and still sacrifices the Saga")
    void chapterIIIWithEmptyGraveyard() {
        Permanent saga = addSagaWithLore(2);
        harness.setGraveyard(player2, List.of());
        Card ownGraveyardCard = new ElspethsNightmare();
        harness.setGraveyard(player1, List.of(ownGraveyardCard));

        triggerNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownGraveyardCard, saga.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chapter III exiles every card type while leaving the controller's graveyard alone")
    void chapterIIIExilesEntireOpponentGraveyard() {
        Card land = new Forest();
        Card enchantment = new ElspethsNightmare();
        Card ownCard = new ElspethsNightmare();
        harness.setGraveyard(player2, List.of(land, enchantment));
        harness.setGraveyard(player1, List.of(ownCard));
        Permanent saga = addSagaWithLore(2);

        triggerNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(land, enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownCard, saga.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chapter I requires a target when an eligible opponent creature exists")
    void chapterICannotBeSkippedWithEligibleTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSagaWithLore(0);

        triggerNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(creature.getId());
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ElspethsNightmare());
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
