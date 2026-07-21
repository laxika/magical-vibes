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

class SangriteBacklashTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Sangrite Backlash targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new SangriteBacklash()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Sangrite Backlash");
    }

    @Test
    @DisplayName("Resolving Sangrite Backlash attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent serra = new Permanent(new SerraAngel());
        gd.playerBattlefields.get(player1.getId()).add(serra);

        harness.setHand(player1, List.of(new SangriteBacklash()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, serra.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sangrite Backlash")
                        && serra.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +3/-3")
    void enchantedCreatureGetsBoostAndDebuff() {
        Permanent serra = new Permanent(new SerraAngel());
        gd.playerBattlefields.get(player1.getId()).add(serra);

        Permanent aura = new Permanent(new SangriteBacklash());
        aura.setAttachedTo(serra.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, serra)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creature returns to base stats when Sangrite Backlash is removed")
    void effectsStopWhenRemoved() {
        Permanent serra = new Permanent(new SerraAngel());
        gd.playerBattlefields.get(player1.getId()).add(serra);

        Permanent aura = new Permanent(new SangriteBacklash());
        aura.setAttachedTo(serra.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, serra)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, serra)).isEqualTo(4);
    }

    @Test
    @DisplayName("Sangrite Backlash fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new SangriteBacklash()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sangrite Backlash"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Sangrite Backlash"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Sangrite Backlash")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SangriteBacklash()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
