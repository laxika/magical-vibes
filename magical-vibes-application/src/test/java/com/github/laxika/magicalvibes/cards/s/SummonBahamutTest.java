package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonBahamut.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class, LlanowarElves.class})
class SummonBahamutTest extends BaseCardTest {

    @Test
    void chapterIDestroysUpToOneTargetNonlandPermanent() {
        harness.addToBattlefield(player1, new SummonBahamut());
        FountainOfYouth fountain = new FountainOfYouth();
        Forest forest = new Forest();
        Permanent fountainPermanent = harness.addToBattlefieldAndReturn(player2, fountain);
        Permanent forestPermanent = harness.addToBattlefieldAndReturn(player2, forest);

        advanceToNextChapter(0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(fountainPermanent.getId());
        assertThat(choice.validIds()).doesNotContain(forestPermanent.getId());

        harness.handlePermanentChosen(player1, fountainPermanent.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(p -> p.getCard().getName())
                .doesNotContain("Fountain of Youth")
                .contains("Forest");
    }

    @Test
    void chapterIIIdrawsTwoCards() {
        Permanent saga = addSagaWithLore(2);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new FountainOfYouth()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    void chapterIVDealsDamageEqualToOtherControlledPermanentsManaValuesToEachOpponent() {
        Permanent saga = addSagaWithLore(3);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonBahamut());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void advanceToNextChapter(int loreCount) {
        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SummonBahamut)
                .findFirst()
                .orElseThrow();
        saga.setCounterCount(CounterType.LORE, loreCount);
        advanceToNextChapter();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
