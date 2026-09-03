package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PantherWarriors;
import com.github.laxika.magicalvibes.cards.w.WaterspoutDjinn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({SuleimansLegacy.class, WaterspoutDjinn.class, ShimmeringEfreet.class, PantherWarriors.class})
class SuleimansLegacyTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys all Djinns and Efreets, spares other creatures")
    void etbDestroysDjinnsAndEfreets() {
        harness.addToBattlefield(player1, new WaterspoutDjinn());
        harness.addToBattlefield(player2, new ShimmeringEfreet());
        harness.addToBattlefield(player1, new PantherWarriors());

        harness.castFromHand(player1, new SuleimansLegacy(), "{R}{W}");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Waterspout Djinn");
        harness.assertNotOnBattlefield(player2, "Shimmering Efreet");
        harness.assertOnBattlefield(player1, "Panther Warriors");
        harness.assertOnBattlefield(player1, "Suleiman's Legacy");
    }

    @Test
    @DisplayName("ETB destruction ignores regeneration shields")
    void etbCannotBeRegenerated() {
        Permanent djinn = harness.addToBattlefieldAndReturn(player2, new WaterspoutDjinn());
        djinn.setRegenerationShield(1);

        harness.castFromHand(player1, new SuleimansLegacy(), "{R}{W}");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Waterspout Djinn");
        harness.assertInGraveyard(player2, "Waterspout Djinn");
    }

    @Test
    @DisplayName("Djinn entering is destroyed and the Legacy enchantment remains")
    void enteringDjinnIsDestroyed() {
        harness.addToBattlefield(player1, new SuleimansLegacy());

        harness.castFromHand(player1, new WaterspoutDjinn(), "{2}{U}{U}");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Waterspout Djinn");
        harness.assertInGraveyard(player1, "Waterspout Djinn");
        // "Destroy it" is the permanent that entered, never the enchantment whose trigger fired.
        harness.assertOnBattlefield(player1, "Suleiman's Legacy");
    }

    @Test
    @DisplayName("Entering Efreet is destroyed and can't be regenerated")
    void enteringEfreetCannotBeRegenerated() {
        harness.addToBattlefield(player1, new SuleimansLegacy());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new ShimmeringEfreet(), "{2}{U}");
        harness.passBothPriorities(); // resolve creature → Legacy trigger queues
        Permanent efreet = findPermanent(player2, "Shimmering Efreet");
        efreet.setRegenerationShield(1);
        harness.passBothPriorities(); // resolve Legacy trigger

        harness.assertNotOnBattlefield(player2, "Shimmering Efreet");
        harness.assertInGraveyard(player2, "Shimmering Efreet");
    }

    @Test
    @DisplayName("Non-Djinn/Efreet entering does not trigger")
    void nonDjinnEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new SuleimansLegacy());

        harness.castFromHand(player1, new PantherWarriors(), "{4}{G}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Panther Warriors");
        org.assertj.core.api.Assertions.assertThat(gd.stack).isEmpty();
    }
}
