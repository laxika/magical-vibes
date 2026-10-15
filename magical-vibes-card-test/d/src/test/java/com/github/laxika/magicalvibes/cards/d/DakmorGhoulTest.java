package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DakmorGhoul.class})
class DakmorGhoulTest extends BaseCardTest {

    @Test
    void etbMakesTargetOpponentLoseTwoLifeAndControllerGainTwoLife() {
        castDakmorGhoul(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void etbDrainWorksWithNonDefaultLifeTotals() {
        harness.setLife(player1, 5);
        harness.setLife(player2, 7);

        castDakmorGhoul(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 7);
        harness.assertLife(player2, 5);
    }

    @Test
    void cannotTargetItsController() {
        harness.setHand(player1, List.of(new DakmorGhoul()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    void lifeTotalsChangeOnlyWhenTheEnterTriggerResolves() {
        castDakmorGhoul(player2.getId());
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Dakmor Ghoul");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void opponentCastingTheGhoulGainsLifeAndDrainsItsOpponent() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DakmorGhoul()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castCreature(player2, 0, player1.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 22);
    }

    @Test
    void enterTriggerStillResolvesAfterTheGhoulLeavesTheBattlefield() {
        castDakmorGhoul(player2.getId());
        harness.passBothPriorities();

        var ghoul = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(ghoul);
        gd.playerGraveyards.get(player1.getId()).add(ghoul.getCard());

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Dakmor Ghoul");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    private void castDakmorGhoul(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new DakmorGhoul()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0, targetId);
    }
}
