package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.l.LavaDart;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlanarChaos.class, LavaDart.class})
class PlanarChaosTest extends BaseCardTest {

    @Test
    @DisplayName("Each spell cast causes a coin flip and counters the spell on a loss")
    void flipsOnAnySpellCastAndCountersOnLoss() {
        harness.addToBattlefield(player1, new PlanarChaos());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new LavaDart()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        boolean won = coinFlipWasWonBy(player2);
        if (won) {
            harness.assertLife(player1, 19);
        } else {
            harness.assertLife(player1, 20);
        }
        harness.assertInGraveyard(player2, "Lava Dart");
    }

    @Test
    @DisplayName("At the beginning of its controller's upkeep, Planar Chaos sacrifices itself on a loss")
    void flipsOnControllerUpkeepAndSacrificesOnLoss() {
        harness.addToBattlefield(player1, new PlanarChaos());
        advanceToUpkeep(player1);
        resolveAllTriggers();

        if (coinFlipWasWonBy(player1)) {
            harness.assertOnBattlefield(player1, "Planar Chaos");
        } else {
            harness.assertInGraveyard(player1, "Planar Chaos");
        }
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

    private boolean coinFlipWasWonBy(Player player) {
        String playerName = player.getUsername();
        boolean won = gameLogContains(playerName + " wins the coin flip for Planar Chaos");
        boolean lost = gameLogContains(playerName + " loses the coin flip for Planar Chaos");
        assertThat(won || lost).as("coin flip should be attributed to %s", playerName).isTrue();
        return won;
    }
}
