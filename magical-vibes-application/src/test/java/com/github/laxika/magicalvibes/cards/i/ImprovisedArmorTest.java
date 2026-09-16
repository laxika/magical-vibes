package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImprovisedArmor.class, Forest.class, ElvishWarrior.class})
class ImprovisedArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Improvised Armor attaches it and boosts the enchanted creature")
    void attachesAndBoostsCreature() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new ImprovisedArmor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, warrior.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> warrior.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(8);
    }

    @Test
    @DisplayName("Resolving Improvised Armor can enchant an opponent's creature")
    void enchantsOpponentsCreature() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        harness.setHand(player1, List.of(new ImprovisedArmor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, warrior.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> warrior.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(8);
    }

    @Test
    @DisplayName("Improvised Armor cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ImprovisedArmor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cycling Improvised Armor discards it and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ImprovisedArmor()));
        harness.setLibrary(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Improvised Armor");
        harness.assertInHand(player1, "Elvish Warrior");
    }

    @Test
    @DisplayName("Cycling Improvised Armor requires three mana")
    void cyclingRequiresThreeMana() {
        harness.setHand(player1, List.of(new ImprovisedArmor()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Improvised Armor");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }
}
