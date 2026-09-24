package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FlaringPain;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlaringPain.class, PlanarChaos.class})
class PlanarChaosTest extends BaseCardTest {

    @Test
    @DisplayName("Each spell cast causes a coin flip and counters the spell on a loss")
    void flipsOnAnySpellCastAndCountersOnLoss() {
        harness.addToBattlefield(player1, new PlanarChaos());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new FlaringPain(), "{1}{R}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        boolean won = gameLogContains(player2.getUsername() + " wins the coin flip for Planar Chaos");
        boolean lost = gameLogContains(player2.getUsername() + " loses the coin flip for Planar Chaos");
        assertThat(won ^ lost)
                .as("Planar Chaos's spell-cast flip must be attributed to the spell's caster")
                .isTrue();
        if (won) {
            assertThat(gqs.isDamagePreventable(gd)).isFalse();
        } else {
            assertThat(gqs.isDamagePreventable(gd)).isTrue();
        }
        harness.assertInGraveyard(player2, "Flaring Pain");
    }

    @Test
    @DisplayName("At the beginning of its controller's upkeep, Planar Chaos sacrifices itself on a loss")
    void flipsOnControllerUpkeepAndSacrificesOnLoss() {
        harness.addToBattlefield(player1, new PlanarChaos());
        advanceToUpkeep(player1);
        resolveAllTriggers();

        boolean won = gameLogContains("wins the coin flip for Planar Chaos");
        boolean lost = gameLogContains("loses the coin flip for Planar Chaos");
        assertThat(won ^ lost).isTrue();
        if (won) {
            harness.assertOnBattlefield(player1, "Planar Chaos");
        } else {
            harness.assertInGraveyard(player1, "Planar Chaos");
        }
    }

    @Test
    @DisplayName("Does not flip a coin during an opponent's upkeep")
    void onlyFlipsDuringItsControllersUpkeep() {
        harness.addToBattlefield(player1, new PlanarChaos());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gameLogContains("coin flip for Planar Chaos")).isFalse();
        harness.assertOnBattlefield(player1, "Planar Chaos");
    }

    @Test
    @DisplayName("Planar Chaos does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new PlanarChaos());
        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Planar Chaos");
        assertThat(gameLogContains("coin flip for Planar Chaos")).isFalse();
    }
}
