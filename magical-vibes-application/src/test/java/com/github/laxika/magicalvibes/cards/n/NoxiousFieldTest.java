package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.h.HollowWarrior;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NoxiousField.class, RhysticCave.class, HollowWarrior.class})
class NoxiousFieldTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land's granted ability deals 1 damage to each creature and each player")
    void grantedAbilityDealsDamageToCreaturesAndPlayers() {
        Permanent land = setUpEnchantedLand();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HollowWarrior());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HollowWarrior());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(ownCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Noxious Field can enchant only a land")
    void cannotEnchantCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HollowWarrior());
        harness.setHand(player1, List.of(new NoxiousField()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("The controller of an enchanted land can activate the granted ability")
    void enchantedLandControllerCanActivateAbility() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RhysticCave());
        harness.setHand(player1, List.of(new NoxiousField()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Noxious Field").getAttachedTo()).isEqualTo(land.getId());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private Permanent setUpEnchantedLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new NoxiousField());
        aura.setAttachedTo(land.getId());
        return land;
    }
}
