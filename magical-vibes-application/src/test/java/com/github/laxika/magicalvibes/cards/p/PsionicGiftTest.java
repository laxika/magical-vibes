package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PsionicGift.class, DuskImp.class, Island.class})
class PsionicGiftTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature can tap to deal 1 damage to a player")
    void grantedAbilityDealsDamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player1, new DuskImp());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PsionicGift());
        aura.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Granted ability deals 1 damage to a target creature")
    void grantedAbilityDealsDamageToCreature() {
        Permanent creature = addCreatureReady(player1, new DuskImp());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PsionicGift());
        aura.setAttachedTo(creature.getId());

        Permanent target = addCreatureReady(player2, new DuskImp());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dusk Imp");
    }

    @Test
    @DisplayName("Creature loses the granted ability when Psionic Gift leaves the battlefield")
    void abilityGoesAwayWhenAuraRemoved() {
        Permanent creature = addCreatureReady(player1, new DuskImp());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PsionicGift());
        aura.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("A summoning-sick enchanted creature cannot activate the granted tap ability")
    void summoningSickCreatureCannotActivateGrantedAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DuskImp());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PsionicGift());
        aura.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Only the enchanted creature receives Psionic Gift's granted ability")
    void onlyEnchantedCreatureGetsGrantedAbility() {
        Permanent enchantedCreature = addCreatureReady(player1, new DuskImp());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PsionicGift());
        aura.setAttachedTo(enchantedCreature.getId());
        Permanent otherCreature = addCreatureReady(player1, new DuskImp());
        int otherCreatureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(otherCreature);

        assertThatThrownBy(() -> harness.activateAbility(player1, otherCreatureIndex, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Psionic Gift can enchant an opponent's creature")
    void canEnchantOpponentCreature() {
        Permanent creature = addCreatureReady(player2, new DuskImp());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PsionicGift());
        aura.setAttachedTo(creature.getId());

        harness.setLife(player1, 20);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Psionic Gift can target only a creature")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new PsionicGift()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent land = findPermanent(player1, "Island");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
