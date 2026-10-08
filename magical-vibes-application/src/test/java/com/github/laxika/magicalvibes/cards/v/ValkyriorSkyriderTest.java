package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValkyriorSkyrider.class})
class ValkyriorSkyriderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains 4 life")
    void etbGainsFourLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 17);
        harness.castFromHand(player1, new ValkyriorSkyrider(), "{4}{W}");
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Life is gained only when the entry trigger resolves")
    void lifeGainWaitsForTriggerResolution() {
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new ValkyriorSkyrider(), "{4}{W}");

        harness.assertLife(player1, 10);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Valkyrior Skyrider");
        harness.assertLife(player1, 10);

        resolveAllTriggers();

        harness.assertLife(player1, 14);
    }

    @Test
    @DisplayName("Entering without being cast gains life for the entering creature's controller")
    void noncastEntryGainsLifeForOpponent() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 17);

        harness.enterBattlefieldAndReturn(player2, new ValkyriorSkyrider());
        resolveAllTriggers();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("The entry trigger still gains life after its source dies")
    void entryTriggerResolvesAfterSourceDies() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 17);
        Permanent skyrider = harness.enterBattlefieldAndReturn(player1, new ValkyriorSkyrider());

        skyrider.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Valkyrior Skyrider");
        harness.assertInGraveyard(player1, "Valkyrior Skyrider");

        resolveAllTriggers();

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 17);
    }
}
