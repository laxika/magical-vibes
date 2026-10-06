package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.EiganjoFreeRiders;
import com.github.laxika.magicalvibes.cards.h.HandOfHonor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reverence.class, HandOfHonor.class, EiganjoFreeRiders.class})
class ReverenceTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures with power 2 or less cannot attack Reverence's controller")
    void preventsSmallCreaturesFromAttackingController() {
        harness.addToBattlefield(player1, new Reverence());
        addCreatureReady(player2, new HandOfHonor());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Creatures with power 3 or greater can attack Reverence's controller")
    void allowsLargerCreaturesToAttackController() {
        harness.addToBattlefield(player1, new Reverence());
        addCreatureReady(player2, new EiganjoFreeRiders());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(player2, List.of(0));

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("A normally small creature can attack when counters raise its power to 3")
    void allowsSmallCreatureWithIncreasedPower() {
        harness.addToBattlefield(player1, new Reverence());
        var attacker = addCreatureReady(player2, new HandOfHonor());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(player2, List.of(0));

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("A flying creature reduced to power 2 cannot attack Reverence's controller")
    void preventsLargerCreatureWithReducedPower() {
        harness.addToBattlefield(player1, new Reverence());
        var attacker = addCreatureReady(player2, new EiganjoFreeRiders());
        attacker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Reverence does not prevent its controller's small creatures from attacking")
    void allowsControllersSmallCreatureToAttackOpponent() {
        addCreatureReady(player1, new HandOfHonor());
        harness.addToBattlefield(player1, new Reverence());
        int lifeBefore = gd.getLife(player2.getId());

        declareAttackers(player1, List.of(0));

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }
}
