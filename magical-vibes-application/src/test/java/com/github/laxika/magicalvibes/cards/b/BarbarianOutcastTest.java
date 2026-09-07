package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarbarianOutcast.class, Swamp.class})
class BarbarianOutcastTest extends BaseCardTest {

    @Test
    @DisplayName("Is sacrificed when its controller controls no Swamps")
    void sacrificedWhenControllerControlsNoSwamps() {
        harness.addToBattlefield(player1, new BarbarianOutcast());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Barbarian Outcast");
        harness.assertInGraveyard(player1, "Barbarian Outcast");
    }

    @Test
    @DisplayName("Survives while its controller controls a Swamp")
    void survivesWithSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new BarbarianOutcast());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Barbarian Outcast");
    }

    @Test
    @DisplayName("Opponent's Swamp does not satisfy the condition")
    void opponentSwampDoesNotCount() {
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player1, new BarbarianOutcast());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Barbarian Outcast");
        harness.assertInGraveyard(player1, "Barbarian Outcast");
    }
}
