package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HormagauntHorde.class, Forest.class})
class HormagauntHordeTest extends BaseCardTest {

    @Test
    @DisplayName("Ravenous enters with X +1/+1 counters without drawing below X=5")
    void ravenousBelowThreshold() {
        castHormagauntHorde(3);

        assertThat(findPermanent(player1, "Hormagaunt Horde").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ravenous draws a card when X is 5 or more")
    void ravenousDrawsAtThreshold() {
        castHormagauntHorde(5);

        assertThat(findPermanent(player1, "Hormagaunt Horde").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("May pay to return itself from the graveyard when a land enters")
    void mayPayToReturnFromGraveyardOnLandfall() {
        HormagauntHorde horde = new HormagauntHorde();
        harness.setGraveyard(player1, List.of(horde));
        prepareMainPhase();
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Hormagaunt Horde");
        harness.assertNotInGraveyard(player1, "Hormagaunt Horde");
    }

    @Test
    @DisplayName("Declining the landfall payment keeps itself in the graveyard")
    void decliningLandfallPaymentKeepsItInGraveyard() {
        HormagauntHorde horde = new HormagauntHorde();
        harness.setGraveyard(player1, List.of(horde));
        prepareMainPhase();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Hormagaunt Horde");
    }

    @Test
    @DisplayName("Ravenous with X zero enters without counters or a draw")
    void ravenousWithZeroX() {
        castHormagauntHorde(0);

        assertThat(findPermanent(player1, "Hormagaunt Horde").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ravenous above the threshold still draws exactly one card")
    void ravenousAboveThreshold() {
        castHormagauntHorde(6);

        assertThat(findPermanent(player1, "Hormagaunt Horde").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(6);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Endless Swarm")
    void opponentsLandDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new HormagauntHorde()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Hormagaunt Horde");
        harness.assertNotInHand(player1, "Hormagaunt Horde");
    }

    @Test
    @DisplayName("Each graveyard copy requires its own payment and returns only itself")
    void graveyardCopiesReturnIndependently() {
        HormagauntHorde first = new HormagauntHorde();
        HormagauntHorde second = new HormagauntHorde();
        harness.setGraveyard(player1, List.of(first, second));
        prepareMainPhase();
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
    }

    @Test
    @DisplayName("Endless Swarm cannot return a card that left and reentered the graveyard")
    void pendingTriggerCannotReturnNewGraveyardObject() {
        HormagauntHorde horde = new HormagauntHorde();
        harness.setGraveyard(player1, List.of(horde));
        gd.markGraveyardEntry(horde);
        prepareMainPhase();
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.playLand(player1, 0);

        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(horde));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(horde));
        gd.markGraveyardEntry(horde);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Hormagaunt Horde");
        harness.assertNotInHand(player1, "Hormagaunt Horde");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private void castHormagauntHorde(int x) {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new HormagauntHorde()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, x);

        harness.castCreature(player1, 0, x);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
