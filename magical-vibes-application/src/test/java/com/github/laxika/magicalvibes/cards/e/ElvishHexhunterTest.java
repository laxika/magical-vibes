package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SanguineBond;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElvishHexhunterTest extends BaseCardTest {

    @Test
    @DisplayName("Ability destroys target enchantment")
    void destroysTargetEnchantment() {
        harness.addToBattlefield(player1, new ElvishHexhunter());
        harness.addToBattlefield(player2, new SanguineBond());
        harness.addMana(player1, ManaColor.GREEN, 1);
        findPermanent(player1, "Elvish Hexhunter").setSummoningSick(false);

        Permanent target = findPermanent(player2, "Sanguine Bond");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Sanguine Bond");
        harness.assertInGraveyard(player2, "Sanguine Bond");
    }

    @Test
    @DisplayName("Elvish Hexhunter is sacrificed when the ability is activated")
    void sacrificedOnActivation() {
        harness.addToBattlefield(player1, new ElvishHexhunter());
        harness.addToBattlefield(player2, new SanguineBond());
        harness.addMana(player1, ManaColor.WHITE, 1);
        findPermanent(player1, "Elvish Hexhunter").setSummoningSick(false);

        Permanent target = findPermanent(player2, "Sanguine Bond");
        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Elvish Hexhunter");
        harness.assertInGraveyard(player1, "Elvish Hexhunter");
    }

    @Test
    @DisplayName("Ability cannot target a non-enchantment")
    void cannotTargetNonEnchantment() {
        harness.addToBattlefield(player1, new ElvishHexhunter());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 1);
        findPermanent(player1, "Elvish Hexhunter").setSummoningSick(false);

        Permanent target = findPermanent(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchantment");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new ElvishHexhunter());
        harness.addToBattlefield(player2, new SanguineBond());
        findPermanent(player1, "Elvish Hexhunter").setSummoningSick(false);

        Permanent target = findPermanent(player2, "Sanguine Bond");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
