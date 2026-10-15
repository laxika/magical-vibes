package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BesottedKnight;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThePrincessTakesFlight.class, BesottedKnight.class})
class ThePrincessTakesFlightTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I exiles up to one target creature and tracks it")
    void chapterIExilesTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());
        addAndResolveSaga();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Besotted Knight");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Besotted Knight"));
    }

    @Test
    @DisplayName("Chapter II gives a creature you control +2/+2 and flying until end of turn")
    void chapterIIBoostsOwnCreatureAndGrantsFlying() {
        harness.addToBattlefield(player1, new ThePrincessTakesFlight());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BesottedKnight());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());
        Permanent saga = findPermanent(player1, "The Princess Takes Flight");
        saga.setCounterCount(CounterType.LORE, 1);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownCreature.getId())
                .doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isEqualTo(2);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(2);
        assertThat(ownCreature.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Chapter III returns the tracked creature under its owner's control")
    void chapterIIIReturnsExiledCreatureToItsOwner() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());
        addAndResolveSaga();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent saga = findPermanent(player1, "The Princess Takes Flight");
        saga.setCounterCount(CounterType.LORE, 2);
        advanceToNextChapter();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Besotted Knight");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Besotted Knight"));
        harness.assertNotOnBattlefield(player1, "Besotted Knight");
        harness.assertInGraveyard(player1, "The Princess Takes Flight");
    }

    @Test
    @DisplayName("Chapter I may target no creature even when creatures are available")
    void chapterICanChooseZeroTargets() {
        harness.addToBattlefield(player2, new BesottedKnight());
        addAndResolveSaga();

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Besotted Knight");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chapter II requires a creature target when a legal creature is available")
    void chapterIICannotChooseZeroTargets() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ThePrincessTakesFlight());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BesottedKnight());
        saga.setCounterCount(CounterType.LORE, 1);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(creature.getId())
                .doesNotContain(player1.getId());
    }

    @Test
    @DisplayName("Chapter II's boost and flying expire at end of turn")
    void chapterIIBonusExpires() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ThePrincessTakesFlight());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BesottedKnight());
        saga.setCounterCount(CounterType.LORE, 1);
        advanceToNextChapter();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        harness.forceStep(TurnStep.CLEANUP);
        gs.advanceStep(gd);

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Chapter III returns all creatures exiled by repeated Chapter I abilities")
    void chapterIIIReturnsAllCardsExiledByThisSaga() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());
        addAndResolveSaga();
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();

        Permanent saga = findPermanent(player1, "The Princess Takes Flight");
        saga.setCounterCount(CounterType.LORE, 0);
        advanceToNextChapter();
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        saga.setCounterCount(CounterType.LORE, 2);
        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Besotted Knight")).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "The Princess Takes Flight");
    }

    @Test
    @DisplayName("Removing the Saga before Chapter III leaves its creature exiled")
    void leavingBeforeChapterIIIDoesNotReturnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());
        addAndResolveSaga();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent saga = findPermanent(player1, "The Princess Takes Flight");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, saga));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "The Princess Takes Flight");
        harness.assertNotOnBattlefield(player2, "Besotted Knight");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Chapter III still returns its exiled creature if the Saga leaves in response")
    void chapterIIIReturnsCreatureAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BesottedKnight());
        addAndResolveSaga();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        Permanent saga = findPermanent(player1, "The Princess Takes Flight");
        saga.setCounterCount(CounterType.LORE, 2);
        advanceToNextChapter();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, saga));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Besotted Knight");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chapter III does nothing when Chapter I exiled no card")
    void chapterIIIWithNoExiledCard() {
        addAndResolveSaga();
        Permanent saga = findPermanent(player1, "The Princess Takes Flight");
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "The Princess Takes Flight");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private void addAndResolveSaga() {
        harness.castFromHand(player1, new ThePrincessTakesFlight(), "{2}{W}");
        harness.passBothPriorities();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
