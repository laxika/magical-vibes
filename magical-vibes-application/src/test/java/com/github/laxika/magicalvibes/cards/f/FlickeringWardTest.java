package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlickeringWard.class, HornedTurtle.class, Mountain.class})
class FlickeringWardTest extends BaseCardTest {

    private Permanent attachWard(Permanent host, CardColor chosenColor) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FlickeringWard());
        aura.setAttachedTo(host.getId());
        aura.setChosenColor(chosenColor);
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature has protection from the chosen color only")
    void enchantedCreatureHasProtectionFromChosenColor() {
        Permanent turtle = addCreatureReady(player1, new HornedTurtle());
        attachWard(turtle, CardColor.BLACK);

        assertThat(gqs.hasProtectionFrom(gd, turtle, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, turtle, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Choosing white does not remove the Aura itself")
    void choosingWhiteDoesNotRemoveTheAura() {
        Permanent turtle = addCreatureReady(player1, new HornedTurtle());
        attachWard(turtle, CardColor.WHITE);

        harness.runStateBasedActions();
        harness.assertOnBattlefield(player1, "Flickering Ward");
        assertThat(gqs.hasProtectionFrom(gd, turtle, CardColor.WHITE)).isTrue();
    }

    @Test
    @DisplayName("Casting it prompts for a colour and grants protection from that colour")
    void castChoosesColorOnEnter() {
        Permanent turtle = addCreatureReady(player1, new HornedTurtle());
        harness.setHand(player1, List.of(new FlickeringWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, turtle.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        harness.assertOnBattlefield(player1, "Flickering Ward");
        assertThat(gqs.hasProtectionFrom(gd, turtle, CardColor.BLUE)).isTrue();
    }

    @Test
    @DisplayName("Can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent turtle = addCreatureReady(player2, new HornedTurtle());
        harness.setHand(player1, List.of(new FlickeringWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, turtle.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        harness.assertOnBattlefield(player1, "Flickering Ward");
        assertThat(gqs.hasProtectionFrom(gd, turtle, CardColor.BLACK)).isTrue();
    }

    @Test
    @DisplayName("{W} returns the Aura to hand and the creature loses protection")
    void activatedAbilityReturnsToHand() {
        Permanent turtle = addCreatureReady(player1, new HornedTurtle());
        attachWard(turtle, CardColor.GREEN);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Flickering Ward");
        harness.assertNotOnBattlefield(player1, "Flickering Ward");
        assertThat(gqs.hasProtectionFrom(gd, turtle, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new FlickeringWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
