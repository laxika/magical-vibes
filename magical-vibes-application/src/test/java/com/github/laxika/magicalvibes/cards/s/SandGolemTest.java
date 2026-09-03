package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindRot;
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

@CardUsed({SandGolem.class, GrizzlyBears.class, MindRot.class, Sift.class})
class SandGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard with a +1/+1 counter at the next end step when discarded by an opponent")
    void returnsWithCounterWhenDiscardedByOpponent() {
        harness.setHand(player2, List.of(new SandGolem(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0); // discard Sand Golem
        harness.handleCardChosen(player2, 0); // discard Grizzly Bears
        harness.passBothPriorities(); // resolve the discard trigger, registering the delayed return

        harness.assertInGraveyard(player2, "Sand Golem");
        harness.assertNotOnBattlefield(player2, "Sand Golem");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities(); // advance to end step and put the delayed trigger on the stack
        harness.passBothPriorities(); // resolve the delayed return

        Permanent returned = findPermanent(player2, "Sand Golem");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(4);
        harness.assertNotInGraveyard(player2, "Sand Golem");
    }

    @Test
    @DisplayName("Does not return if it left the graveyard before the end step")
    void doesNotReturnIfNoLongerInGraveyard() {
        harness.setHand(player2, List.of(new SandGolem(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Sand Golem");
        gd.playerGraveyards.get(player2.getId()).clear();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities(); // advance to end step and put the delayed trigger on the stack
        harness.passBothPriorities(); // resolve the delayed trigger

        harness.assertNotOnBattlefield(player2, "Sand Golem");
    }

    @Test
    @DisplayName("Waits for the delayed end-step trigger to resolve before returning")
    void waitsForDelayedTriggerToResolve() {
        harness.setHand(player2, List.of(new SandGolem(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).isNotEmpty();
        harness.assertInGraveyard(player2, "Sand Golem");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Sand Golem");
    }

    @Test
    @DisplayName("Does not return when its own controller discards it")
    void doesNotReturnOnSelfDiscard() {
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        harness.setHand(player1, List.of(new Sift(), new SandGolem()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // Sift draws 3 and prompts for the discard
        harness.handleCardChosen(player1, 0); // discard Sand Golem

        harness.assertInGraveyard(player1, "Sand Golem");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sand Golem");
        harness.assertInGraveyard(player1, "Sand Golem");
    }
}
