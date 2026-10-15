package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed(MandroidSquadron.class)
class MandroidSquadronTest extends BaseCardTest {

    @Test
    @DisplayName("Casting it gains life only after its enter trigger resolves")
    void castingGainsLifeOnlyWhenEnterTriggerResolves() {
        harness.setLife(player1, 8);
        harness.setLife(player2, 17);

        harness.castFromHand(player1, new MandroidSquadron(), "{1}{W}");
        harness.assertLife(player1, 8);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Mandroid Squadron");
        harness.assertLife(player1, 8);

        resolveAllTriggers();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Each entering copy creates its own life gain trigger")
    void eachEnteringCopyGainsTwoLife() {
        harness.setLife(player1, 8);
        harness.setLife(player2, 17);

        harness.enterBattlefieldAndReturn(player1, new MandroidSquadron());
        harness.enterBattlefieldAndReturn(player1, new MandroidSquadron());
        harness.assertLife(player1, 8);

        resolveAllTriggers();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("When it enters, its controller gains 2 life")
    void enteringGivesItsControllerTwoLife() {
        harness.setLife(player1, 8);
        harness.setLife(player2, 17);

        harness.enterBattlefieldAndReturn(player1, new MandroidSquadron());
        resolveAllTriggers();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Its enter-the-battlefield ability gives life to the permanent's controller")
    void enteringUnderPlayerTwoGivesPlayerTwoLife() {
        harness.setLife(player1, 17);
        harness.setLife(player2, 8);

        harness.enterBattlefieldAndReturn(player2, new MandroidSquadron());
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 10);
    }
}
