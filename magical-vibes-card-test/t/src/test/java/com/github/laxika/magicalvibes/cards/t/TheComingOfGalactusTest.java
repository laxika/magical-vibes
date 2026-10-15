package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheComingOfGalactus.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class, TrollAscetic.class})
class TheComingOfGalactusTest extends BaseCardTest {

    @Test
    void chapterICanChooseNoTargetEvenWhenNonlandPermanentsExist() {
        Permanent saga = addSagaWithLore(0);
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(player1.getId(), fountain.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(fountain);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    void chapterITriggersWhenTheSagaEntersAndCanDestroyItself() {
        harness.castFromHand(player1, new TheComingOfGalactus(), "{2}{B}{B}{G}");
        harness.passBothPriorities();

        Permanent saga = findPermanent(player1, "The Coming of Galactus");
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(saga.getId());

        harness.handlePermanentChosen(player1, saga.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "The Coming of Galactus");
        harness.assertInGraveyard(player1, "The Coming of Galactus");
    }

    @Test
    void chapterIExcludesOpposingHexproofPermanentsButAllowsControlledOnes() {
        addSagaWithLore(0);
        Permanent opposingTroll = harness.addToBattlefieldAndReturn(player2, new TrollAscetic());
        Permanent controlledTroll = harness.addToBattlefieldAndReturn(player1, new TrollAscetic());

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(controlledTroll.getId()).doesNotContain(opposingTroll.getId());

        harness.handlePermanentChosen(player1, controlledTroll.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(controlledTroll);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingTroll);
    }

    @Test
    void chapterIVDoesNotSacrificeTheSagaBeforeItsAbilityResolves() {
        Permanent saga = addSagaWithLore(3);

        advanceToNextChapter();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        assertThat(countPermanents(player1, "Galactus")).isZero();
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Galactus")).isEqualTo(1);
        harness.assertInGraveyard(player1, "The Coming of Galactus");
    }

    @Test
    void lifeLossChaptersDoNotChangeTheirControllersLife() {
        addSagaWithLore(1);
        int controllerLife = gd.playerLifeTotals.get(player1.getId());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        advanceToNextChapter();
        harness.passBothPriorities();
        advanceToNextChapter();
        harness.passBothPriorities();

        harness.assertLife(player1, controllerLife);
        harness.assertLife(player2, opponentLife - 4);
    }

    @Test
    void galactusMustTargetALandEvenWhenOnlyItsControllerHasOne() {
        addSagaWithLore(3);
        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent galactus = findPermanent(player1, "Galactus");
        galactus.setSummoningSick(false);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(player1, java.util.List.of(gd.playerBattlefields.get(player1.getId()).indexOf(galactus)));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(forest.getId());

        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void chapterIDestroysUpToOneTargetNonlandPermanent() {
        addSagaWithLore(0);
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(fountain.getId()).doesNotContain(forest.getId());

        harness.handlePermanentChosen(player1, fountain.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(forest).doesNotContain(fountain);
    }

    @Test
    void chapterIICausesEachOpponentToLoseTwoLife() {
        addSagaWithLore(1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void chapterIIICausesEachOpponentToLoseTwoLife() {
        addSagaWithLore(2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void chapterIVCreatesTheLegendaryGalactusToken() {
        Permanent saga = addSagaWithLore(3);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent galactus = findPermanent(player1, "Galactus");
        assertThat(galactus.getCard().getPower()).isEqualTo(16);
        assertThat(galactus.getCard().getToughness()).isEqualTo(16);
        assertThat(galactus.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(galactus.getCard().getSubtypes()).contains(CardSubtype.ELDER, CardSubtype.ALIEN);
        assertThat(galactus.getCard().getKeywords()).contains(Keyword.FLYING, Keyword.TRAMPLE);
        assertThat(galactus.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @Test
    void GalactusDestroysOnlyAChosenLandWhenItAttacks() {
        addSagaWithLore(3);
        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent galactus = findPermanent(player1, "Galactus");
        galactus.setSummoningSick(false);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(player1, java.util.List.of(gd.playerBattlefields.get(player1.getId()).indexOf(galactus)));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(forest.getId()).doesNotContain(bears.getId());

        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(forest).contains(bears);
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheComingOfGalactus());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
