package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ArborElf;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GaladrielElvenQueen.class, ArborElf.class, Forest.class})
class GaladrielElvenQueenTest extends BaseCardTest {

    @Test
    void dominionMajorityTemptsTheRingAndCountersTheChosenRingBearer() {
        Permanent galadriel = harness.addToBattlefieldAndReturn(player1, new GaladrielElvenQueen());
        harness.enterBattlefieldAndReturn(player1, new ArborElf());

        advanceToBeginningOfCombat(player1);

        assertThat(activeVote().playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, ChoiceContext.GaladrielElvenQueenChoice.DOMINION);
        harness.handleListChoice(player2, ChoiceContext.GaladrielElvenQueenChoice.DOMINION);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, galadriel.getId());

        assertThat(gd.ringLevels).containsEntry(player1.getId(), 1);
        assertThat(gd.ringBearerIds).containsEntry(player1.getId(), galadriel.getId());
        assertThat(galadriel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void guidanceWinsOnTieAndDrawsACard() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addToBattlefield(player1, new GaladrielElvenQueen());
        harness.enterBattlefieldAndReturn(player1, new ArborElf());

        advanceToBeginningOfCombat(player1);

        harness.handleListChoice(player1, ChoiceContext.GaladrielElvenQueenChoice.DOMINION);
        harness.handleListChoice(player2, ChoiceContext.GaladrielElvenQueenChoice.GUIDANCE);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.ringLevels).doesNotContainKey(player1.getId());
    }

    @Test
    void doesNotTriggerWithoutAnotherElfEnteringThisTurn() {
        harness.addToBattlefield(player1, new GaladrielElvenQueen());

        advanceToBeginningOfCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private PendingInteraction.ColorChoice activeVote() {
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyElementsOf(ChoiceContext.GaladrielElvenQueenChoice.OPTIONS);
        return choice;
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
