package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HarmattanEfreet;
import com.github.laxika.magicalvibes.cards.n.NettletoothDjinn;
import com.github.laxika.magicalvibes.cards.a.ArcaneAdaptation;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({SuleimansLegacy.class, HarmattanEfreet.class, NettletoothDjinn.class, GrizzlyBears.class,
        ArcaneAdaptation.class})
class SuleimansLegacyTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys all Djinns and Efreets, spares other creatures")
    void etbDestroysDjinnsAndEfreets() {
        harness.addToBattlefield(player1, new NettletoothDjinn());
        harness.addToBattlefield(player2, new HarmattanEfreet());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castLegacy();
        harness.passBothPriorities(); // resolve Legacy
        harness.passBothPriorities(); // resolve ETB wipe

        harness.assertNotOnBattlefield(player1, "Nettletooth Djinn");
        harness.assertNotOnBattlefield(player2, "Harmattan Efreet");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Suleiman's Legacy");
    }

    @Test
    @DisplayName("ETB destruction ignores regeneration shields")
    void etbCannotBeRegenerated() {
        Permanent djinn = harness.addToBattlefieldAndReturn(player2, new NettletoothDjinn());
        djinn.setRegenerationShield(1);

        castLegacy();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Nettletooth Djinn");
        harness.assertInGraveyard(player2, "Nettletooth Djinn");
    }

    @Test
    @DisplayName("Djinn entering is destroyed; non-Djinn/Efreet is not")
    void enteringDjinnIsDestroyed() {
        harness.addToBattlefield(player1, new SuleimansLegacy());

        harness.castFromHand(player1, new NettletoothDjinn(), "{3}{G}");
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve Legacy trigger

        harness.assertNotOnBattlefield(player1, "Nettletooth Djinn");
        harness.assertInGraveyard(player1, "Nettletooth Djinn");
        // "Destroy it" is the permanent that entered, never the enchantment whose trigger fired.
        harness.assertOnBattlefield(player1, "Suleiman's Legacy");
    }

    @Test
    @DisplayName("Entering Efreet is destroyed and can't be regenerated")
    void enteringEfreetCannotBeRegenerated() {
        harness.addToBattlefield(player1, new SuleimansLegacy());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new HarmattanEfreet(), "{2}{U}{U}");
        harness.passBothPriorities(); // resolve creature → Legacy trigger queues
        Permanent efreet = findPermanent(player2, "Harmattan Efreet");
        efreet.setRegenerationShield(1);
        harness.passBothPriorities(); // resolve Legacy trigger

        harness.assertNotOnBattlefield(player2, "Harmattan Efreet");
        harness.assertInGraveyard(player2, "Harmattan Efreet");
    }

    @Test
    @DisplayName("Non-Djinn/Efreet entering does not trigger")
    void nonDjinnEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new SuleimansLegacy());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        org.assertj.core.api.Assertions.assertThat(gd.stack).isEmpty();
    }

    private void castLegacy() {
        harness.castFromHand(player1, new SuleimansLegacy(), "{R}{W}");
    }

    @Test
    @DisplayName("Opponent's creature entering as a Djinn is destroyed")
    void opponentCreatureWithGrantedDjinnTypeIsDestroyed() {
        harness.addToBattlefield(player1, new SuleimansLegacy());
        Permanent adaptation = harness.addToBattlefieldAndReturn(player2, new ArcaneAdaptation());
        adaptation.setChosenSubtype(CardSubtype.DJINN);
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Legacy controller's type grant does not affect an opponent's entering creature")
    void ownTypeGrantDoesNotDestroyOpponentCreature() {
        harness.addToBattlefield(player1, new SuleimansLegacy());
        Permanent adaptation = harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation());
        adaptation.setChosenSubtype(CardSubtype.EFREET);
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        org.assertj.core.api.Assertions.assertThat(gd.stack).isEmpty();
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
