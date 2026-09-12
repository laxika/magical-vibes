package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinRaider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlanchwoodArmor.class, Forest.class, GoblinRaider.class})
class BlanchwoodArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Blanchwood Armor puts it on the stack")
    void castingPutsOnStack() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new GoblinRaider());
        harness.setHand(player1, List.of(new BlanchwoodArmor()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, raider.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Blanchwood Armor attaches it and grants +1/+1 per Forest you control")
    void resolvesAndBoostsPerForest() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new GoblinRaider());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new BlanchwoodArmor()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0, raider.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof BlanchwoodArmor
                        && raider.getId().equals(p.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(4);
    }

    @Test
    @DisplayName("Blanchwood Armor updates dynamically when Forest count changes")
    void updatesDynamicallyWithForestCount() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new GoblinRaider());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new BlanchwoodArmor());
        armor.setAttachedTo(raider.getId());

        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(2);

        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(3);

        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof Forest);
        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(2);
    }

    @Test
    @DisplayName("Blanchwood Armor counts Forests controlled by aura controller, even on opponent creature")
    void countsAurasControllersForests() {
        Permanent opponentRaider = harness.addToBattlefieldAndReturn(player2, new GoblinRaider());

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        Permanent armor = harness.addToBattlefieldAndReturn(player1, new BlanchwoodArmor());
        armor.setAttachedTo(opponentRaider.getId());

        assertThat(gqs.getEffectivePower(gd, opponentRaider)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentRaider)).isEqualTo(4);
    }

    @Test
    @DisplayName("Blanchwood Armor effect ends when aura leaves battlefield")
    void effectEndsWhenAuraLeavesBattlefield() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new GoblinRaider());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        Permanent armor = harness.addToBattlefieldAndReturn(player1, new BlanchwoodArmor());
        armor.setAttachedTo(raider.getId());

        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(armor);

        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Blanchwood Armor")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new BlanchwoodArmor()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Blanchwood Armor fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemovedBeforeResolution() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new GoblinRaider());
        harness.setHand(player1, List.of(new BlanchwoodArmor()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, raider.getId());
        gd.playerBattlefields.get(player1.getId()).remove(raider);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof BlanchwoodArmor);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof BlanchwoodArmor);
    }
}
