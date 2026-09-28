package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BarrinsCodex;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WurmcoilEngine;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OriginOfIronMan.class, BarrinsCodex.class, GrizzlyBears.class, WurmcoilEngine.class})
class OriginOfIronManTest extends BaseCardTest {

    @Test
    void chapterITapsAndLocksAnOptionalCreatureTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSagaWithLore(0);

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getUntapPreventedWhileSourceOnBattlefieldIds()).isNotEmpty();
    }

    @Test
    void chapterIAllowsChoosingNoCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSagaWithLore(0);

        triggerChapter();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getUntapPreventedWhileSourceOnBattlefieldIds()).isEmpty();
    }

    @Test
    void chapterIIDrawsTwoCards() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addSagaWithLore(1);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void chapterIIIOnlyPutsAnEligibleArtifactFromHandOntoTheBattlefield() {
        WurmcoilEngine tooExpensiveArtifact = new WurmcoilEngine();
        GrizzlyBears nonArtifact = new GrizzlyBears();
        BarrinsCodex eligibleArtifact = new BarrinsCodex();
        harness.setHand(player1, List.of(tooExpensiveArtifact, nonArtifact, eligibleArtifact));
        addSagaWithLore(2);

        triggerChapter();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.HandCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(choice.validIndices()).containsExactly(2);
        harness.handleCardChosen(player1, 2);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == eligibleArtifact);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(tooExpensiveArtifact, nonArtifact);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new OriginOfIronMan());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
