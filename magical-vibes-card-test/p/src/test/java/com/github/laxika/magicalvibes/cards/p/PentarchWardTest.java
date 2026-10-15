package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
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

@CardUsed({PentarchWard.class, AshcoatBear.class, PrismaticLens.class})
class PentarchWardTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a color draws a card and grants protection from that color")
    void choosingColorDrawsAndGrantsProtection() {
        Permanent bear = addCreatureReady(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new PentarchWard()));
        harness.setLibrary(player1, List.of(new AshcoatBear()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ashcoat Bear");
        assertThat(gqs.hasProtectionFrom(gd, bear, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, bear, CardColor.BLUE)).isFalse();
        assertThat(findPermanent(player1, "Pentarch Ward").getAttachedTo()).isEqualTo(bear.getId());
    }

    @Test
    @DisplayName("Protection from white does not remove Pentarch Ward")
    void protectionFromItsColorDoesNotRemoveAura() {
        Permanent bear = addCreatureReady(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new PentarchWard()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");
        harness.passBothPriorities();

        Permanent ward = findPermanent(player1, "Pentarch Ward");
        assertThat(gqs.hasProtectionFrom(gd, bear, CardColor.WHITE)).isTrue();
        assertThat(ward.getAttachedTo()).isEqualTo(bear.getId());
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PrismaticLens());
        harness.setHand(player1, List.of(new PentarchWard()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanting an opponent's creature draws for the Aura's controller")
    void enchantingOpponentsCreatureDrawsForAuraController() {
        Permanent bear = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new PentarchWard()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new PrismaticLens()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        harness.assertNotInHand(player1, "Prismatic Lens");
        assertThat(gqs.hasProtectionFrom(gd, bear, CardColor.GREEN)).isTrue();
        assertThat(findPermanent(player1, "Pentarch Ward").getAttachedTo()).isEqualTo(bear.getId());

        harness.passBothPriorities();

        harness.assertInHand(player1, "Prismatic Lens");
        harness.assertNotInHand(player2, "Prismatic Lens");
    }

    @Test
    @DisplayName("The Aura's exception does not allow another white Aura to target the creature")
    void whiteProtectionPreventsAnotherWardFromTargeting() {
        Permanent bear = addCreatureReady(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new PentarchWard()));
        harness.setLibrary(player1, List.of(new PrismaticLens()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new PentarchWard()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Pentarch Ward")).isEqualTo(1);
        assertThat(findPermanent(player1, "Pentarch Ward").getAttachedTo()).isEqualTo(bear.getId());
    }
}
