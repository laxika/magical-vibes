package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.e.EnigmaEidolon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TasteForMayhem.class, EnigmaEidolon.class, AzoriusSignet.class})
class TasteForMayhemTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+0 with a card in your hand")
    void enchantedCreatureGetsBaseBoost() {
        Permanent eidolon = addEidolon(player1);
        harness.setHand(player1, List.of(new TasteForMayhem(), new AzoriusSignet()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, eidolon.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, eidolon)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, eidolon)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enchanted creature gets an additional +2/+0 with an empty hand")
    void enchantedCreatureGetsHellbentBoost() {
        Permanent eidolon = addEidolon(player1);
        harness.setHand(player1, List.of(new TasteForMayhem()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, eidolon.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, eidolon)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, eidolon)).isEqualTo(2);
    }

    @Test
    @DisplayName("The hellbent boost changes as your hand changes")
    void hellbentBoostIsDynamic() {
        Permanent eidolon = addEidolon(player1);
        harness.setHand(player1, List.of(new TasteForMayhem()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, eidolon.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, eidolon)).isEqualTo(6);

        harness.setHand(player1, List.of(new AzoriusSignet()));
        assertThat(gqs.getEffectivePower(gd, eidolon)).isEqualTo(4);
    }

    @Test
    @DisplayName("Hellbent checks the Aura controller's hand on an opponent's creature")
    void hellbentUsesAuraControllerHand() {
        Permanent eidolon = addEidolon(player2);
        harness.setHand(player1, List.of(new TasteForMayhem()));
        harness.setHand(player2, List.of(new AzoriusSignet()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, eidolon.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, eidolon)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, eidolon)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost ends when Taste for Mayhem leaves the battlefield")
    void boostEndsWhenAuraLeaves() {
        Permanent eidolon = addEidolon(player1);
        harness.setHand(player1, List.of(new TasteForMayhem()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, eidolon.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Taste for Mayhem");

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, eidolon)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, eidolon)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent signet = harness.addToBattlefieldAndReturn(player1, new AzoriusSignet());
        harness.setHand(player1, List.of(new TasteForMayhem()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, signet.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addEidolon(Player player) {
        return harness.addToBattlefieldAndReturn(player, new EnigmaEidolon());
    }
}
