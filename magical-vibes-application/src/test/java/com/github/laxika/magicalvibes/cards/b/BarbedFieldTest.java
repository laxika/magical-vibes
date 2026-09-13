package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WintermoonMesa;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarbedField.class, WintermoonMesa.class, BrandedBrawlers.class})
class BarbedFieldTest extends BaseCardTest {

    @Test
    @DisplayName("Barbed Field enchants a land and grants it a damage ability")
    void enchantsLandAndGrantsAbility() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());
        harness.setHand(player1, List.of(new BarbedField()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Barbed Field");
        assertThat(aura.getAttachedTo()).isEqualTo(land.getId());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted land can deal damage to a creature")
    void dealsDamageToCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BarbedField());
        aura.setAttachedTo(land.getId());
        Permanent brawlers = harness.addToBattlefieldAndReturn(player2, new BrandedBrawlers());

        harness.activateAbility(player1, 0, 2, null, brawlers.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(brawlers);
        assertThat(brawlers.getMarkedDamage()).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can enchant an opponent's land, whose controller can activate the ability")
    void enchantsOpponentsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new WintermoonMesa());
        harness.setHand(player1, List.of(new BarbedField()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        harness.activateAbility(player2, 0, 2, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Barbed Field cannot enchant a nonland permanent")
    void rejectsNonlandTarget() {
        Permanent brawlers = harness.addToBattlefieldAndReturn(player1, new BrandedBrawlers());
        harness.setHand(player1, List.of(new BarbedField()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, brawlers.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Granted damage ability disappears when Barbed Field leaves")
    void abilityDisappearsWhenAuraLeaves() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BarbedField());
        aura.setAttachedTo(land.getId());
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
