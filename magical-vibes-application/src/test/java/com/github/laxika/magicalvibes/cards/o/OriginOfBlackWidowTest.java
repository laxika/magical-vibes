package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OriginOfBlackWidow.class, Forest.class, GrizzlyBears.class, Opalescence.class})
class OriginOfBlackWidowTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I makes each opponent choose and sacrifice a creature")
    void chapterISacrificesAnOpponentCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSagaWithLore(0);

        triggerNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(first.getId(), second.getId());

        harness.handlePermanentChosen(player2, first.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(second).doesNotContain(first);
    }

    @Test
    @DisplayName("Chapter II gives your creatures deathtouch until end of turn")
    void chapterIIGivesDeathtouchUntilEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSagaWithLore(1);

        triggerNextChapter();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.DEATHTOUCH)).isFalse();

        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Chapter III counts only creature cards in each opponent's graveyard")
    void chapterIIILosesLifeForCreatureCardsInOpponentGraveyard() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Forest(), new GrizzlyBears()));
        int lifeBefore = gd.getLife(player2.getId());
        addSagaWithLore(2);

        triggerNextChapter();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new OriginOfBlackWidow());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    @Test
    void chapterILeavesBothPlayersNonCreaturePermanentsAndYourCreaturesAlone() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        addSagaWithLore(0);

        triggerNextChapter();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opposingLand);
    }

    @Test
    void chapterIIDoesNotGrantDeathtouchToCreaturesEnteringAfterResolution() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addSagaWithLore(1);

        triggerNextChapter();
        Permanent later = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, existing, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, later, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void chapterIIIncludesTheSagaWhenItIsACreature() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent saga = addSagaWithLore(1);
        assertThat(gqs.isCreature(gd, saga)).isTrue();

        triggerNextChapter();

        assertThat(gqs.hasKeyword(gd, saga, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void chapterIIIWithNoOpposingCreatureCardsDoesNotLoseLifeAndSacrificesSaga() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Forest()));
        int ownLife = gd.getLife(player1.getId());
        int opposingLife = gd.getLife(player2.getId());
        Permanent saga = addSagaWithLore(2);

        triggerNextChapter();

        assertThat(gd.getLife(player1.getId())).isEqualTo(ownLife);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opposingLife);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        harness.assertInGraveyard(player1, "Origin of Black Widow");
    }

    @Test
    void enteringTriggersChapterIAndSacrificesTheOnlyOpposingCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new OriginOfBlackWidow(), "{2}{B}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Origin of Black Widow");
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
