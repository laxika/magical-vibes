package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AntiMagicAura.class, GrizzlyBears.class, HolyStrength.class, Incinerate.class,
        ProdigalSorcerer.class})
class AntiMagicAuraTest extends BaseCardTest {

    private Permanent enchantedBears() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AntiMagicAura());
        aura.setAttachedTo(bears.getId());
        return bears;
    }

    @Test
    @DisplayName("Enchanted creature can't be the target of spells")
    void cannotBeTargetedBySpells() {
        Permanent bears = enchantedBears();

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of spells");
    }

    @Test
    @DisplayName("Enchanted creature can't be the target of its controller's spells")
    void cannotBeTargetedByItsControllersSpells() {
        Permanent bears = enchantedBears();

        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of spells");
    }

    @Test
    @DisplayName("Enchanted creature can't be enchanted by another Aura")
    void cannotBeEnchantedByAnotherAura() {
        Permanent bears = enchantedBears();

        harness.setHand(player2, List.of(new HolyStrength()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature can still be the target of abilities")
    void canBeTargetedByAbilities() {
        addCreatureReady(player1, new ProdigalSorcerer());
        Permanent bears = enchantedBears();

        harness.activateAbility(player1, 0, null, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Prodigal Sorcerer"));
    }

    @Test
    @DisplayName("Without the Aura the creature can be targeted by spells")
    void targetableWithoutAura() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Incinerate"));
    }

    @Test
    @DisplayName("Anti-Magic Aura resolves and remains attached to its target")
    void resolvesAndRemainsAttached() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new AntiMagicAura()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.runStateBasedActions();

        assertThat(findPermanent(player1, "Anti-Magic Aura").getAttachedTo()).isEqualTo(bears.getId());
        harness.assertNotInGraveyard(player1, "Anti-Magic Aura");
    }

    @Test
    @DisplayName("Other Auras already enchanting the creature go to their owners' graveyards")
    void removesExistingAurasFromBothPlayers() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent friendlyAura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        friendlyAura.setAttachedTo(bears.getId());
        Permanent opposingAura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        opposingAura.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new AntiMagicAura()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Holy Strength");
        harness.assertNotOnBattlefield(player2, "Holy Strength");
        harness.assertInGraveyard(player1, "Holy Strength");
        harness.assertInGraveyard(player2, "Holy Strength");
        assertThat(findPermanent(player1, "Anti-Magic Aura").getAttachedTo()).isEqualTo(bears.getId());
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Targeted abilities resolve normally on the enchanted creature")
    void targetedAbilityDealsDamage() {
        addCreatureReady(player1, new ProdigalSorcerer());
        Permanent bears = enchantedBears();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Anti-Magic Aura");
    }
}
