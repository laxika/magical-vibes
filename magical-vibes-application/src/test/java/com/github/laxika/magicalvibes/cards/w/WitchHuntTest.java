package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.r.RenewedFaith;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WitchHunt.class, RenewedFaith.class})
class WitchHuntTest extends BaseCardTest {

    @Test
    void dealsFourDamageToItsControllerOnTheirUpkeep() {
        harness.addToBattlefield(player1, new WitchHunt());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    void preventsLifeGainForBothPlayers() {
        harness.addToBattlefield(player1, new WitchHunt());
        harness.setHand(player2, List.of(new RenewedFaith()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    void transfersControlToTheOnlyOpponentAtItsControllerEndStep() {
        harness.addToBattlefield(player1, new WitchHunt());

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Witch Hunt")).isEmpty();
        assertThat(findPermanents(player2, "Witch Hunt")).hasSize(1);

        advanceToEndStep(player2);

        assertThat(findPermanents(player1, "Witch Hunt")).hasSize(1);
        assertThat(findPermanents(player2, "Witch Hunt")).isEmpty();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
