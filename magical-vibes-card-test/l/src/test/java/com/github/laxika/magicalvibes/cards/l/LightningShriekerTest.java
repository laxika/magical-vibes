package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightningShrieker.class})
class LightningShriekerTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.setLibrary(player1, new ArrayList<>());
        harness.setLibrary(player2, new ArrayList<>());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Owner shuffles it into their library at every end step")
    void shufflesIntoOwnersLibraryAtEveryEndStep() {
        Card card = new LightningShrieker();
        card.setOwnerId(player2.getId());
        Permanent shrieker = harness.addToBattlefieldAndReturn(player1, card);
        gd.stolenCreatures.put(shrieker.getId(), player2.getId());

        advanceToEndStep(player2);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lightning Shrieker");
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Lightning Shrieker"));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Lightning Shrieker"));
    }

    @Test
    @DisplayName("Shuffles into its owner's library on its controller's end step")
    void shufflesOnControllerEndStep() {
        LightningShrieker card = new LightningShrieker();
        harness.addToBattlefield(player1, card);

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Lightning Shrieker");
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        harness.assertNotOnBattlefield(player1, "Lightning Shrieker");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each copy shuffles itself without moving a copy that entered after the end step began")
    void eachTriggerOnlyShufflesItsOwnSource() {
        LightningShrieker first = new LightningShrieker();
        LightningShrieker second = new LightningShrieker();
        LightningShrieker lateArrival = new LightningShrieker();
        harness.addToBattlefield(player1, first);
        harness.addToBattlefield(player1, second);

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(2);
        Permanent latePermanent = harness.enterBattlefieldAndReturn(player1, lateArrival);
        assertThat(gd.stack).hasSize(2);

        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(latePermanent);
    }

    @Test
    @DisplayName("The trigger does not shuffle a creature that left the battlefield")
    void doesNotShuffleCreatureThatLeftBattlefield() {
        LightningShrieker card = new LightningShrieker();
        Permanent shrieker = harness.addToBattlefieldAndReturn(player1, card);
        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, shrieker));
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returning to the battlefield does not let the old trigger shuffle the new permanent")
    void oldTriggerDoesNotShuffleReturnedCreature() {
        LightningShrieker card = new LightningShrieker();
        Permanent shrieker = harness.addToBattlefieldAndReturn(player1, card);
        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, shrieker));
        gd.playerHands.get(player1.getId()).remove(card);
        Permanent returned = harness.enterBattlefieldAndReturn(player1, card);
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(returned);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
