package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed(SpiritualGuardian.class)
class SpiritualGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, controller gains 4 life")
    void gainsFourLifeOnEnter() {
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new SpiritualGuardian()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("When an opponent casts it, only that opponent gains 4 life")
    void gainsLifeForItsController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new SpiritualGuardian()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 24);
    }
}
