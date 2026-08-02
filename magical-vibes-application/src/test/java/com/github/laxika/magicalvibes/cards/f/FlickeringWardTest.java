package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlickeringWardTest extends BaseCardTest {

    private Permanent attachWard(Permanent host, CardColor chosenColor) {
        Permanent aura = new Permanent(new FlickeringWard());
        aura.setAttachedTo(host.getId());
        aura.setChosenColor(chosenColor);
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature has protection from the chosen color only")
    void enchantedCreatureHasProtectionFromChosenColor() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachWard(bears, CardColor.BLACK);

        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Choosing white does not remove the Aura itself")
    void choosingWhiteDoesNotRemoveTheAura() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachWard(bears, CardColor.WHITE);

        boolean changed = GameTestEngineContext.get().getBean(PermanentRemovalService.class)
                .enforceAttachmentLegality(gd);

        assertThat(changed).isFalse();
        harness.assertOnBattlefield(player1, "Flickering Ward");
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.WHITE)).isTrue();
    }

    @Test
    @DisplayName("Casting it prompts for a colour and grants protection from that colour")
    void castChoosesColorOnEnter() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlickeringWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        harness.assertOnBattlefield(player1, "Flickering Ward");
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.BLUE)).isTrue();
    }

    @Test
    @DisplayName("{W} returns the Aura to hand and the creature loses protection")
    void activatedAbilityReturnsToHand() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachWard(bears, CardColor.GREEN);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Flickering Ward");
        harness.assertNotOnBattlefield(player1, "Flickering Ward");
        assertThat(gqs.hasProtectionFrom(gd, bears, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.m.Mountain());
        harness.setHand(player1, List.of(new FlickeringWard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
