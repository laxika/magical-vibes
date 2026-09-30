package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({GrismoldTheDreadsower.class, GrizzlyBears.class, Shock.class})
class GrismoldTheDreadsowerTest extends BaseCardTest {

    @Test
    @DisplayName("At your end step, each player creates a Plant token")
    void eachPlayerCreatesPlantTokenAtEndStep() {
        harness.addToBattlefield(player1, new GrismoldTheDreadsower());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Plant")).hasSize(1);
        assertThat(findPermanents(player2, "Plant")).hasSize(1);
    }

    @Test
    @DisplayName("A creature token dying puts a +1/+1 counter on Grismold")
    void creatureTokenDeathPutsCounterOnGrismold() {
        Permanent grismold = harness.addToBattlefieldAndReturn(player1, new GrismoldTheDreadsower());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        Permanent token = findPermanents(player2, "Plant").getFirst();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, token.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(grismold.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A nontoken creature dying does not put a counter on Grismold")
    void nontokenCreatureDeathDoesNotPutCounterOnGrismold() {
        Permanent grismold = harness.addToBattlefieldAndReturn(player1, new GrismoldTheDreadsower());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(grismold.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
