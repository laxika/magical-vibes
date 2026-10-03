package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({ThunderingMightmare.class, GrizzlyBears.class})
class ThunderingMightmareTest extends BaseCardTest {

    @Test
    @DisplayName("While paired, an opponent's spell puts a counter on both creatures")
    void opponentSpellPutsCountersOnBothPairedCreatures() {
        Permanent bears = pairWithBears();
        Permanent mightmare = findPermanent(player1, "Thundering Mightmare");

        castOpponentCreature();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(mightmare.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An unpaired Thundering Mightmare does not trigger")
    void unpairedDoesNotTrigger() {
        Permanent mightmare = harness.addToBattlefieldAndReturn(player1, new ThunderingMightmare());
        castOpponentCreature();
        harness.passBothPriorities();

        assertThat(mightmare.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The controller's spell does not trigger the paired ability")
    void controllerSpellDoesNotTrigger() {
        Permanent bears = pairWithBears();
        Permanent mightmare = findPermanent(player1, "Thundering Mightmare");
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(mightmare.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent pairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ThunderingMightmare()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private void castOpponentCreature() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
    }
}
