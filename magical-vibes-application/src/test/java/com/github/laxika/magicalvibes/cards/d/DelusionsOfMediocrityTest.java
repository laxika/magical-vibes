package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed(DelusionsOfMediocrity.class)
class DelusionsOfMediocrityTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gains 10 life")
    void entryGainsTenLife() {
        harness.castFromHand(player1, new DelusionsOfMediocrity(), "{3}{U}");
        resolveAllTriggers();

        harness.assertLife(player1, 30);
    }

    @Test
    @DisplayName("Leaving the battlefield loses 10 life")
    void leavingLosesTenLife() {
        Permanent delusions = harness.addToBattlefieldAndReturn(player1, new DelusionsOfMediocrity());
        harness.setLife(player1, 30);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, delusions));
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Leaving the battlefield for exile loses 10 life")
    void exilingLosesTenLife() {
        Permanent delusions = harness.addToBattlefieldAndReturn(player1, new DelusionsOfMediocrity());
        harness.setLife(player1, 30);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, delusions));
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }
}
