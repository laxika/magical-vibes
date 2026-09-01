package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.Disallow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IvoryGargoyle.class})
class IvoryGargoyleTest extends BaseCardTest {

    @Test
    @DisplayName("Dies, returns to the battlefield at the next end step")
    void diesThenReturnsAtNextEndStep() {
        killGargoyle(player1);

        harness.assertInGraveyard(player1, "Ivory Gargoyle");
        harness.assertNotOnBattlefield(player1, "Ivory Gargoyle");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities(); // advance to the end step, processing the delayed return

        harness.assertOnBattlefield(player1, "Ivory Gargoyle");
        harness.assertNotInGraveyard(player1, "Ivory Gargoyle");
    }

    @Test
    @DisplayName("Dies, controller skips their next draw step")
    void diesThenControllerSkipsNextDrawStep() {
        killGargoyle(player1);

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2; // avoid the first-turn skip
        harness.forceStep(TurnStep.UPKEEP);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance UPKEEP → DRAW, runs handleDrawStep

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("The death trigger queues a draw-step skip on its controller and nothing else")
    void queuesNothingButTheControllersDrawStepSkip() {
        killGargoyle(player1);

        assertThat(gd.skipNextDrawStepCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.skipNextDrawStepCount.getOrDefault(player2.getId(), 0)).isEqualTo(0);
        assertThat(gd.skipNextTurnCount).isEmpty();
        assertThat(gd.skipNextUntapStepCount).isEmpty();
        assertThat(gd.skipNextCombatPhaseCount).isEmpty();
    }

    @Test
    @DisplayName("Only one draw step is skipped per death")
    void skipsOnlyOneDrawStep() {
        killGargoyle(player1);

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // skipped draw step

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // the following draw step draws normally

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Activated ability exiles it, so it never dies and never returns")
    void activatedAbilityExilesIt() {
        harness.addToBattlefield(player1, new IvoryGargoyle());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ivory Gargoyle");
        harness.assertNotInGraveyard(player1, "Ivory Gargoyle");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Ivory Gargoyle"));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ivory Gargoyle");
    }

    @Test
    @CardUsed({Disallow.class})
    @DisplayName("Countering the death ability counters both of its instructions")
    void counteringDeathAbilityCountersBothInstructions() {
        harness.setHand(player2, List.of(new Disallow()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        Permanent gargoyle = harness.addToBattlefieldAndReturn(player1, new IvoryGargoyle());
        gargoyle.setMarkedDamage(2);
        harness.runStateBasedActions();

        harness.passPriority(player1);
        harness.castInstant(player2, 0, gargoyle.getCard().getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Ivory Gargoyle");
        assertThat(gd.skipNextDrawStepCount.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Returns to its owner's battlefield and skips its controller's draw step")
    void returnsToOwnerAndSkipsControllerDrawStep() {
        IvoryGargoyle card = new IvoryGargoyle();
        card.setOwnerId(player1.getId());
        Permanent gargoyle = harness.addToBattlefieldAndReturn(player2, card);
        gd.stolenCreatures.put(gargoyle.getId(), player1.getId());
        killGargoyle(gargoyle);

        harness.assertNotOnBattlefield(player2, "Ivory Gargoyle");
        int handBefore = gd.playerHands.get(player2.getId()).size();
        int deckBefore = gd.playerDecks.get(player2.getId()).size();
        assertThat(gd.skipNextDrawStepCount.getOrDefault(player2.getId(), 0)).isEqualTo(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ivory Gargoyle");
        assertThat(gd.skipNextDrawStepCount.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.skipNextDrawStepCount.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore);
    }

    private void killGargoyle(Player player) {
        killGargoyle(harness.addToBattlefieldAndReturn(player, new IvoryGargoyle()));
    }

    private void killGargoyle(Permanent gargoyle) {
        gargoyle.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities(); // resolve the skip-draw-step death trigger
        harness.passBothPriorities(); // resolve the delayed-return death trigger
    }
}
