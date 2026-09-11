package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.f.Fervor;
import com.github.laxika.magicalvibes.cards.s.SerrasBlessing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenalishInfantry.class, Fervor.class, SerrasBlessing.class, TranquilGrove.class})
class TranquilGroveTest extends BaseCardTest {

    private void payCost() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Destroys other enchantments but not itself")
    void destroysOtherEnchantmentsButNotItself() {
        harness.addToBattlefield(player1, new TranquilGrove());
        harness.addToBattlefield(player1, new Fervor());
        payCost();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fervor");
        harness.assertInGraveyard(player1, "Fervor");
        harness.assertOnBattlefield(player1, "Tranquil Grove");
    }

    @Test
    @DisplayName("Destroys enchantments controlled by both players")
    void destroysEnchantmentsFromBothPlayers() {
        harness.addToBattlefield(player1, new TranquilGrove());
        harness.addToBattlefield(player1, new Fervor());
        harness.addToBattlefield(player2, new SerrasBlessing());
        payCost();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fervor");
        harness.assertNotOnBattlefield(player2, "Serra's Blessing");
    }

    @Test
    @DisplayName("Does not destroy creatures")
    void doesNotDestroyCreatures() {
        harness.addToBattlefield(player1, new TranquilGrove());
        harness.addToBattlefield(player1, new BenalishInfantry());
        payCost();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Benalish Infantry");
    }

    @Test
    @DisplayName("Requires two green mana to activate")
    void requiresTwoGreenManaToActivate() {
        harness.addToBattlefield(player1, new TranquilGrove());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Assertions.assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys another Tranquil Grove but not the activating copy")
    void destroysAnotherCopyButNotActivatingCopy() {
        harness.addToBattlefield(player1, new TranquilGrove());
        harness.addToBattlefield(player1, new TranquilGrove());
        harness.addToBattlefield(player1, new Fervor());
        payCost();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Assertions.assertThat(countPermanents(player1, "Tranquil Grove")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Tranquil Grove");
    }
}
