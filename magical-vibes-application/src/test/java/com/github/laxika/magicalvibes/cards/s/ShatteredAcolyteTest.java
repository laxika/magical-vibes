package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShatteredAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Ability destroys target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player1, new ShatteredAcolyte());
        harness.addToBattlefield(player2, new IronMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent target = findPermanent(player2, "Iron Myr");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Iron Myr");
        harness.assertInGraveyard(player2, "Iron Myr");
    }

    @Test
    @DisplayName("Ability destroys target enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player1, new ShatteredAcolyte());
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent target = findPermanent(player2, "Angelic Chorus");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Shattered Acolyte is sacrificed as a cost")
    void sacrificedAsCost() {
        harness.addToBattlefield(player1, new ShatteredAcolyte());
        harness.addToBattlefield(player2, new IronMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent target = findPermanent(player2, "Iron Myr");
        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Shattered Acolyte");
        harness.assertInGraveyard(player1, "Shattered Acolyte");
    }

    @Test
    @DisplayName("Ability cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new ShatteredAcolyte());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent target = findPermanent(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or enchantment");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new ShatteredAcolyte());
        harness.addToBattlefield(player2, new IronMyr());

        Permanent target = findPermanent(player2, "Iron Myr");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
