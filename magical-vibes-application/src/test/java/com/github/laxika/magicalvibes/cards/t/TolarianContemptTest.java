package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TolarianContempt.class, GrizzlyBears.class, Island.class})
class TolarianContemptTest extends BaseCardTest {

    @Test
    @DisplayName("Entering puts a rejection counter on each opponent creature")
    void enteringPutsRejectionCounterOnEachOpponentCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castTolarianContempt();

        assertThat(ownCreature.getCounterCount(CounterType.REJECTION)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.REJECTION)).isEqualTo(1);
    }

    @Test
    @DisplayName("At the controller's end step it targets one marked creature per opponent")
    void endStepTargetsOneMarkedCreaturePerOpponent() {
        Permanent markedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card existingTop = new Island();
        harness.setLibrary(player2, List.of(existingTop));

        castTolarianContempt();
        Permanent unmarkedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(markedCreature.getId())
                .doesNotContain(unmarkedCreature.getId());

        harness.handlePermanentChosen(player1, markedCreature.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);

        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(unmarkedCreature)
                .doesNotContain(markedCreature);
        assertThat(gd.playerHands.get(player2.getId())).contains(existingTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(markedCreature.getCard());
    }

    private void castTolarianContempt() {
        harness.setHand(player1, List.of(new TolarianContempt()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
