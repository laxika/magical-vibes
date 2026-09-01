package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonGFCerberus.class, GrizzlyBears.class, Shock.class})
class SummonGFCerberusTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I surveils 1")
    void chapterISurveilsOne() {
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new SummonGFCerberus()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Chapter II copies the next instant once")
    void chapterIICopiesNextInstantOnce() {
        addSagaWithLoreCounters(1);
        advanceToChapter();
        harness.passBothPriorities();

        castShockWithPendingCopies(1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Chapter III copies the next instant twice")
    void chapterIIICopiesNextInstantTwice() {
        addSagaWithLoreCounters(2);
        advanceToChapter();
        harness.passBothPriorities();

        castShockWithPendingCopies(2);

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    private void addSagaWithLoreCounters(int count) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonGFCerberus());
        saga.setCounterCount(CounterType.LORE, count);
    }

    private void advanceToChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void castShockWithPendingCopies(int copyCount) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        for (int i = 0; i < copyCount; i++) {
            harness.handleMayAbilityChosen(player1, false);
        }
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
