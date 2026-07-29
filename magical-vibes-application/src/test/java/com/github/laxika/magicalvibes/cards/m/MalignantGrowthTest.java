package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MalignantGrowthTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW
    }

    @Test
    @DisplayName("Opponent draws one extra card per growth counter and takes that much damage")
    void opponentDrawsAndTakesDamage() {
        Permanent growth = harness.addToBattlefieldAndReturn(player1, new MalignantGrowth());
        growth.setCounterCount(CounterType.GROWTH, 2);

        int handBefore = gd.playerHands.get(player2.getId()).size();
        int deckBefore = gd.playerDecks.get(player2.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToDraw(player2);
        harness.passBothPriorities(); // resolve the draw-step trigger

        // Normal draw + 2 extra draws
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Does not trigger on the controller's own draw step")
    void doesNotTriggerOnControllersDrawStep() {
        Permanent growth = harness.addToBattlefieldAndReturn(player1, new MalignantGrowth());
        growth.setCounterCount(CounterType.GROWTH, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToDraw(player1);

        // Only the normal draw, no damage
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("With no growth counters the opponent only makes their normal draw")
    void noCountersMeansNoExtraDrawsOrDamage() {
        harness.addToBattlefield(player1, new MalignantGrowth());

        int handBefore = gd.playerHands.get(player2.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToDraw(player2);
        harness.passBothPriorities(); // resolve the draw-step trigger

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Upkeep adds a growth counter when cumulative upkeep is paid")
    void upkeepAddsGrowthCounter() {
        Permanent growth = harness.addToBattlefieldAndReturn(player1, new MalignantGrowth());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve the growth counter trigger
        harness.passBothPriorities(); // resolve the cumulative upkeep trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(growth.getCounterCount(CounterType.AGE)).isEqualTo(1);
        assertThat(growth.getCounterCount(CounterType.GROWTH)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(growth);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Malignant Growth")
    void decliningCumulativeUpkeepSacrifices() {
        Permanent growth = harness.addToBattlefieldAndReturn(player1, new MalignantGrowth());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve the growth counter trigger
        harness.passBothPriorities(); // resolve the cumulative upkeep trigger
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(growth);
        harness.assertInGraveyard(player1, "Malignant Growth");
    }
}
