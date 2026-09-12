package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GlitteringLion;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.cards.w.WellOfDiscovery;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunkenField.class, RhysticCave.class, WellOfDiscovery.class, GlitteringLion.class})
class SunkenFieldTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land can counter a spell when its controller cannot pay {1}")
    void countersSpellWhenControllerCannotPay() {
        Permanent land = addEnchantedLand();

        WellOfDiscovery spell = new WellOfDiscovery();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, spell, "{6}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, 1, null, spell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Well of Discovery");
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted land does not counter a spell when its controller pays {1}")
    void spellResolvesWhenControllerPays() {
        addEnchantedLand();

        WellOfDiscovery spell = new WellOfDiscovery();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, spell, "{6}");
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, 1, null, spell.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Well of Discovery");
    }

    @Test
    @DisplayName("Sunken Field cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player1, new RhysticCave());
        Permanent lion = harness.addToBattlefieldAndReturn(player1, new GlitteringLion());
        harness.setHand(player1, List.of(new SunkenField()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, lion.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Controller of an enchanted land can activate the granted ability")
    void enchantedLandControllerCanActivateAbility() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RhysticCave());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SunkenField());
        aura.setAttachedTo(land.getId());

        WellOfDiscovery spell = new WellOfDiscovery();
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, spell, "{6}");
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, 1, null, spell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Well of Discovery");
        assertThat(land.isTapped()).isTrue();
    }

    private Permanent addEnchantedLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SunkenField());
        aura.setAttachedTo(land.getId());
        return land;
    }
}
