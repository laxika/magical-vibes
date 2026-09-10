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

@CardUsed({TheComingOfGalactus.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class})
class TheComingOfGalactusTest extends BaseCardTest {

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
