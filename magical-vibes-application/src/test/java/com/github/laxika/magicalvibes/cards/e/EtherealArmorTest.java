package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AzoriusKeyrune;
import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.s.SphereOfSafety;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EtherealArmor.class, DrudgeBeetle.class, SphereOfSafety.class, AzoriusKeyrune.class})
class EtherealArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Ethereal Armor counts itself, so a lone Ethereal Armor gives +1/+1 and first strike")
    void countsItself() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());

        harness.setHand(player1, List.of(new EtherealArmor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Ethereal Armor")
                        && bears.getId().equals(p.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Ethereal Armor scales with other enchantments you control")
    void scalesWithOtherEnchantments() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new EtherealArmor());
        armor.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        harness.addToBattlefield(player1, new SphereOfSafety());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        harness.addToBattlefield(player1, new SphereOfSafety());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Ethereal Armor ignores enchantments controlled by the opponent")
    void ignoresOpponentEnchantments() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new EtherealArmor());
        armor.setAttachedTo(bears.getId());

        harness.addToBattlefield(player2, new SphereOfSafety());
        harness.addToBattlefield(player2, new SphereOfSafety());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ethereal Armor's boost and first strike end when the aura leaves the battlefield")
    void effectEndsWhenAuraLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new EtherealArmor());
        armor.setAttachedTo(bears.getId());
        harness.addToBattlefield(player1, new SphereOfSafety());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(armor);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent with Ethereal Armor")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new DrudgeBeetle());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AzoriusKeyrune());
        harness.setHand(player1, List.of(new EtherealArmor()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("An opposing enchanted creature uses the Aura controller's enchantment count")
    void enchantsOpponentCreatureUsingAuraControllersCount() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DrudgeBeetle());
        harness.addToBattlefield(player1, new SphereOfSafety());
        harness.addToBattlefield(player2, new SphereOfSafety());
        harness.addToBattlefield(player2, new SphereOfSafety());
        harness.setHand(player1, List.of(new EtherealArmor()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Two Ethereal Armors each count both Auras, and the remaining Aura keeps first strike")
    void multipleArmorsStackAndRecalculateWhenOneLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EtherealArmor());
        first.setAttachedTo(creature.getId());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new EtherealArmor());
        second.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Nonenchantment permanents do not increase Ethereal Armor's bonus")
    void ignoresNonenchantmentPermanents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new EtherealArmor());
        armor.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new AzoriusKeyrune());
        harness.addToBattlefield(player1, new DrudgeBeetle());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }
}
