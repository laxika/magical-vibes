package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlanarChaos.class, Shock.class})
class PlanarChaosTest extends BaseCardTest {

    @Test
    @DisplayName("Each spell cast causes a coin flip and counters the spell on a loss")
    void flipsOnAnySpellCastAndCountersOnLoss() {
        harness.addToBattlefield(player1, new PlanarChaos());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        boolean won = coinFlipWasWon();
        if (won) {
            harness.assertLife(player1, 18);
        } else {
            harness.assertLife(player1, 20);
        }
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("At the beginning of its controller's upkeep, Planar Chaos sacrifices itself on a loss")
    void flipsOnControllerUpkeepAndSacrificesOnLoss() {
        harness.addToBattlefield(player1, new PlanarChaos());
        advanceToUpkeep(player1);
        resolveAllTriggers();

        if (coinFlipWasWon()) {
            harness.assertOnBattlefield(player1, "Planar Chaos");
        } else {
            harness.assertInGraveyard(player1, "Planar Chaos");
        }
    }

    private boolean coinFlipWasWon() {
        return gd.gameLog.stream().map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("wins the coin flip for Planar Chaos"));
    }
}
