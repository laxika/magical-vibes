package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TriassicEggTest extends BaseCardTest {

    private static final String HAND_MODE = "You may put a creature card from your hand onto the battlefield.";
    private static final String GRAVEYARD_MODE = "Return target creature card from your graveyard to the battlefield.";

    @Test
    @DisplayName("The first ability puts a hatchling counter on Triassic Egg")
    void putsHatchlingCounter() {
        Permanent egg = addEgg();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(egg.getCounterCount(CounterType.HATCHLING)).isEqualTo(1);
    }

    @Test
    @DisplayName("The sacrifice ability requires two hatchling counters")
    void requiresTwoHatchlingCounters() {
        Permanent egg = addEgg();
        egg.setCounterCount(CounterType.HATCHLING, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The hand mode may put a creature onto the battlefield")
    void handMode() {
        Permanent egg = addEgg();
        egg.setCounterCount(CounterType.HATCHLING, 2);
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(creature));

        activateSacrificeAbility();
        harness.handleListChoice(player1, HAND_MODE);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Triassic Egg");
    }

    @Test
    @DisplayName("The graveyard mode returns a creature card to the battlefield")
    void graveyardMode() {
        Permanent egg = addEgg();
        egg.setCounterCount(CounterType.HATCHLING, 2);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        activateSacrificeAbility();
        harness.handleListChoice(player1, GRAVEYARD_MODE);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    private Permanent addEgg() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return harness.addToBattlefieldAndReturn(player1, new TriassicEgg());
    }

    private void activateSacrificeAbility() {
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
    }
}
