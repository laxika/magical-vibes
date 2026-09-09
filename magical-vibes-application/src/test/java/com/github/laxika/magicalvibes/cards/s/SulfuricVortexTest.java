package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RenewedFaith;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({SulfuricVortex.class, RenewedFaith.class})
class SulfuricVortexTest extends BaseCardTest {

    @Test
    void dealsTwoDamageToBothPlayersOnControllerUpkeep() {
        harness.addToBattlefield(player1, new SulfuricVortex());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void dealsTwoDamageToBothPlayersOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new SulfuricVortex());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void preventsControllerLifeGain() {
        harness.addToBattlefield(player1, new SulfuricVortex());
        harness.setHand(player1, List.of(new RenewedFaith()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    void preventsOpponentLifeGain() {
        harness.addToBattlefield(player1, new SulfuricVortex());
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
}
