package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class PresenceOfTheMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an enchantment spell cast by an opponent")
    void countersOpponentsEnchantmentSpell() {
        harness.addToBattlefield(player1, new PresenceOfTheMaster());
        harness.setHand(player2, List.of(new AngelicChorus()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Angelic Chorus");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Counters an enchantment spell cast by its controller")
    void countersControllersEnchantmentSpell() {
        harness.addToBattlefield(player1, new PresenceOfTheMaster());
        harness.setHand(player1, List.of(new AngelicChorus()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Angelic Chorus");
        harness.assertNotOnBattlefield(player1, "Angelic Chorus");
    }

    @Test
    @DisplayName("Does not trigger for a non-enchantment spell")
    void doesNotTriggerForNonEnchantmentSpell() {
        harness.addToBattlefield(player1, new PresenceOfTheMaster());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
