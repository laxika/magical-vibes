package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HarmattanEfreet;
import com.github.laxika.magicalvibes.cards.n.NettletoothDjinn;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

        harness.setHand(player1, List.of(new NettletoothDjinn()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
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

        harness.setHand(player2, List.of(new HarmattanEfreet()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
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

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        org.assertj.core.api.Assertions.assertThat(gd.stack).isEmpty();
    }

    private void castLegacy() {
        harness.setHand(player1, List.of(new SuleimansLegacy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0);
    }
}
