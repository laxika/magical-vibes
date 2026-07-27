package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TranquilGroveTest extends BaseCardTest {

    private void payCost() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Destroys other enchantments but not itself")
    void destroysOtherEnchantmentsButNotItself() {
        harness.addToBattlefield(player1, new TranquilGrove());
        harness.addToBattlefield(player1, new RuleOfLaw());
        payCost();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rule of Law");
        harness.assertInGraveyard(player1, "Rule of Law");
        harness.assertOnBattlefield(player1, "Tranquil Grove");
    }

    @Test
    @DisplayName("Destroys enchantments controlled by both players")
    void destroysEnchantmentsFromBothPlayers() {
        harness.addToBattlefield(player1, new TranquilGrove());
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.addToBattlefield(player2, new AngelicChorus());
        payCost();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rule of Law");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Does not destroy creatures")
    void doesNotDestroyCreatures() {
        harness.addToBattlefield(player1, new TranquilGrove());
        harness.addToBattlefield(player1, new GrizzlyBears());
        payCost();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
