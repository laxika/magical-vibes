package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonAnima.class, Forest.class, GrizzlyBears.class})
class SummonAnimaTest extends BaseCardTest {

    @Test
    void chaptersIThroughIIIDrawAndLoseLife() {
        Permanent saga = addSagaWithLore(0);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        resolveNextChapter(saga, 0);
        resolveNextChapter(saga, 1);
        resolveNextChapter(saga, 2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    @Test
    void chapterIVMakesEachOpponentChooseACreatureAndLoseLife() {
        Permanent saga = addSagaWithLore(3);
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToNextChapter();
        harness.passBothPriorities();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstCreature.getId(), secondCreature.getId());

        harness.handlePermanentChosen(player2, firstCreature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(secondCreature)
                .doesNotContain(firstCreature);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 3);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonAnima());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void resolveNextChapter(Permanent saga, int lore) {
        saga.setCounterCount(CounterType.LORE, lore);
        advanceToNextChapter();
        harness.passBothPriorities();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
