package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SanguineBond;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvishHexhunter.class, LlanowarElves.class, SanguineBond.class})
class ElvishHexhunterTest extends BaseCardTest {

    @Test
    @DisplayName("Ability destroys target enchantment")
    void destroysTargetEnchantment() {
        addCreatureReady(player1, new ElvishHexhunter());
        harness.addToBattlefield(player2, new SanguineBond());
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent target = findPermanent(player2, "Sanguine Bond");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Sanguine Bond");
        harness.assertInGraveyard(player2, "Sanguine Bond");
    }

    @Test
    @DisplayName("Elvish Hexhunter is sacrificed when the ability is activated")
    void sacrificedOnActivation() {
        addCreatureReady(player1, new ElvishHexhunter());
        harness.addToBattlefield(player2, new SanguineBond());
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent target = findPermanent(player2, "Sanguine Bond");
        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Elvish Hexhunter");
        harness.assertInGraveyard(player1, "Elvish Hexhunter");
    }

    @Test
    @DisplayName("Ability cannot target a non-enchantment")
    void cannotTargetNonEnchantment() {
        addCreatureReady(player1, new ElvishHexhunter());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent target = findPermanent(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchantment");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new ElvishHexhunter());
        harness.addToBattlefield(player2, new SanguineBond());

        Permanent target = findPermanent(player2, "Sanguine Bond");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("White mana pays the hybrid cost and the ability can destroy your own enchantment")
    void whiteManaDestroysOwnEnchantment() {
        addCreatureReady(player1, new ElvishHexhunter());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SanguineBond());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Elvish Hexhunter");
        harness.assertOnBattlefield(player1, "Sanguine Bond");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sanguine Bond");
        harness.assertInGraveyard(player1, "Sanguine Bond");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new ElvishHexhunter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SanguineBond());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        harness.assertOnBattlefield(player1, "Elvish Hexhunter");
        harness.assertOnBattlefield(player2, "Sanguine Bond");
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent hexhunter = addCreatureReady(player1, new ElvishHexhunter());
        hexhunter.setTapped(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SanguineBond());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.assertOnBattlefield(player1, "Elvish Hexhunter");
        harness.assertOnBattlefield(player2, "Sanguine Bond");
    }

    @Test
    @DisplayName("Blue mana cannot pay the hybrid cost")
    void cannotActivateWithWrongColorMana() {
        addCreatureReady(player1, new ElvishHexhunter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SanguineBond());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Elvish Hexhunter");
        harness.assertOnBattlefield(player2, "Sanguine Bond");
    }

    @Test
    @DisplayName("A second activation destroying the target does not refund the first sacrifice")
    void targetGoneBeforeResolutionDoesNotRefundSacrifice() {
        addCreatureReady(player1, new ElvishHexhunter());
        addCreatureReady(player1, new ElvishHexhunter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SanguineBond());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.activateAbility(player1, 0, 0, null, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Elvish Hexhunter");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player2, "Sanguine Bond");
        harness.assertInGraveyard(player2, "Sanguine Bond");
    }
}
