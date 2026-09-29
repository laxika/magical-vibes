package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonIxion.class, GrizzlyBears.class})
class SummonIxionTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I exiles an opponent's creature until the Saga leaves")
    void chapterIExilesOpponentCreatureUntilSagaLeaves() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent saga = addSagaWithLore(0);

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(opponentCreature.getId()).doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentCreature.getCard());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, saga));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(opponentCreature.getCard());
    }

    @Test
    @DisplayName("Chapter II puts counters on up to two creatures you control and gains life")
    void chapterIIPutsCountersOnOwnCreaturesAndGainsLife() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSagaWithLore(1);

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(firstCreature.getId(), secondCreature.getId())
                .doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, firstCreature.getId());
        harness.handlePermanentChosen(player1, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Chapter III repeats the counter and life-gain effect")
    void chapterIIIPutsCounterAndGainsLife() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addSagaWithLore(2);

        triggerChapter();

        harness.handlePermanentChosen(player1, firstCreature.getId());
        harness.handlePermanentChosen(player1, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 22);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonIxion());
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
