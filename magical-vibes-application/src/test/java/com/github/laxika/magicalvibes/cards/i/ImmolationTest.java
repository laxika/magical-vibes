package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImmolationTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Immolation targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent giant = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player1.getId()).add(giant);

        harness.setHand(player1, List.of(new Immolation()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, giant.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Immolation");
    }

    @Test
    @DisplayName("Resolving Immolation attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent giant = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player1.getId()).add(giant);

        harness.setHand(player1, List.of(new Immolation()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Immolation")
                        && giant.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/-2")
    void enchantedCreatureGetsBoost() {
        Permanent giant = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player1.getId()).add(giant);

        Permanent aura = new Permanent(new Immolation());
        aura.setAttachedTo(giant.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creature returns to base stats when Immolation is removed")
    void effectsStopWhenRemoved() {
        Permanent giant = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player1.getId()).add(giant);

        Permanent aura = new Permanent(new Immolation());
        aura.setAttachedTo(giant.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Immolation fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent giant = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player1.getId()).add(giant);

        harness.setHand(player1, List.of(new Immolation()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, giant.getId());
        gd.playerBattlefields.get(player1.getId()).remove(giant);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Immolation"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Immolation"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Immolation")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Immolation()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
