package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TattooWardTest extends BaseCardTest {

    private Permanent attachWard(Permanent creature) {
        Permanent aura = new Permanent(new TattooWard());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and protection from enchantments")
    void enchantedCreatureGetsBoostAndProtection() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent enchantment = new Permanent(new GloriousAnthem());
        gd.playerBattlefields.get(player2.getId()).add(enchantment);
        attachWard(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, bears, enchantment)).isTrue();
    }

    @Test
    @DisplayName("Protection from enchantments does not remove Tattoo Ward")
    void protectionDoesNotRemoveThisAura() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachWard(bears);

        boolean changed = GameTestEngineContext.get().getBean(PermanentRemovalService.class)
                .enforceAttachmentLegality(gd);

        assertThat(changed).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
    }

    @Test
    @DisplayName("Sacrificing Tattoo Ward destroys target enchantment")
    void sacrificeDestroysTargetEnchantment() {
        harness.addToBattlefield(player1, new TattooWard());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Glorious Anthem"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Tattoo Ward");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Sacrifice ability cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new TattooWard());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enchantment");
    }
}
