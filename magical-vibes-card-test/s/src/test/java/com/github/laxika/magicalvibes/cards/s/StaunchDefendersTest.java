package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StaunchDefenders.class})
class StaunchDefendersTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield queues life gain until the trigger resolves")
    void entryQueuesLifeGainUntilTriggerResolves() {
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new StaunchDefenders(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);

        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 4);
    }

    @Test
    @DisplayName("Resolving the ETB trigger gains 4 life")
    void entryGainsFourLife() {
        harness.castFromHand(player1, new StaunchDefenders(), "{3}{W}{W}");
        resolveAllTriggers();

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("ETB life gain affects only the creature's controller")
    void entryGainsLifeForControllerOnly() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 17);

        harness.castFromHand(player1, new StaunchDefenders(), "{3}{W}{W}");
        resolveAllTriggers();

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Entering the battlefield puts the life-gain trigger on the stack")
    void entryTriggersLifeGain() {
        harness.castFromHand(player1, new StaunchDefenders(), "{3}{W}{W}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Staunch Defenders");
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Entering without being cast gains life for the entering creature's controller")
    void entryWithoutCastingGainsLifeForController() {
        harness.setLife(player1, 11);
        harness.setLife(player2, 16);

        harness.enterBattlefieldAndReturn(player2, new StaunchDefenders());

        harness.assertLife(player2, 16);
        resolveAllTriggers();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each entering copy triggers its own four life gain")
    void eachEnteringCopyGainsFourLife() {
        harness.enterBattlefieldAndReturn(player1, new StaunchDefenders());
        harness.enterBattlefieldAndReturn(player1, new StaunchDefenders());

        harness.assertLife(player1, 20);
        resolveAllTriggers();

        harness.assertLife(player1, 28);
        harness.assertLife(player2, 20);
    }
}
