package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SiegecraftTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Siegecraft targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new Siegecraft()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Siegecraft");
    }

    @Test
    @DisplayName("Resolving Siegecraft attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new Siegecraft()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Siegecraft")
                        && bears.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+4")
    void enchantedCreatureGetsBoost() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent aura = new Permanent(new Siegecraft());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
    }

    @Test
    @DisplayName("Creature returns to base stats when Siegecraft is removed")
    void effectsStopWhenRemoved() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent aura = new Permanent(new Siegecraft());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Siegecraft fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new Siegecraft()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Siegecraft");
        harness.assertNotOnBattlefield(player1, "Siegecraft");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Siegecraft")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Siegecraft()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
