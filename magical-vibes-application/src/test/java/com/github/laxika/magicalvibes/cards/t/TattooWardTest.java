package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DivineSacrament;
import com.github.laxika.magicalvibes.cards.e.EmberBeast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TattooWard.class, EmberBeast.class, DivineSacrament.class})
class TattooWardTest extends BaseCardTest {

    private Permanent attachWard(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TattooWard());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and protection from enchantments")
    void enchantedCreatureGetsBoostAndProtection() {
        Permanent beast = addCreatureReady(player1, new EmberBeast());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new DivineSacrament());
        attachWard(beast);

        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(5);
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, beast, enchantment)).isTrue();
    }

    @Test
    @DisplayName("Protection from enchantments does not remove Tattoo Ward")
    void protectionDoesNotRemoveThisAura() {
        Permanent beast = addCreatureReady(player1, new EmberBeast());
        Permanent aura = attachWard(beast);

        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
    }

    @Test
    @DisplayName("Can be cast targeting a creature")
    void canBeCastTargetingCreature() {
        Permanent beast = addCreatureReady(player1, new EmberBeast());
        harness.setHand(player1, List.of(new TattooWard()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, beast.getId());
        harness.passBothPriorities();

        Permanent ward = findPermanent(player1, "Tattoo Ward");
        assertThat(ward.getAttachedTo()).isEqualTo(beast.getId());
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNoncreature() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new DivineSacrament());
        harness.setHand(player1, List.of(new TattooWard()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Sacrificing Tattoo Ward destroys target enchantment")
    void sacrificeDestroysTargetEnchantment() {
        harness.addToBattlefield(player1, new TattooWard());
        harness.addToBattlefield(player2, new DivineSacrament());
        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Divine Sacrament"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Tattoo Ward");
        harness.assertNotOnBattlefield(player2, "Divine Sacrament");
        harness.assertInGraveyard(player2, "Divine Sacrament");
    }

    @Test
    @DisplayName("Sacrifice ability cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new TattooWard());
        harness.addToBattlefield(player2, new EmberBeast());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, harness.getPermanentId(player2, "Ember Beast")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enchantment");
    }
}
