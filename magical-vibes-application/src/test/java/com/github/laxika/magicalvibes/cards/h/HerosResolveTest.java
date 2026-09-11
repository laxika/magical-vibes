package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({HerosResolve.class, GrizzlyBears.class, FountainOfYouth.class})
class HerosResolveTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Hero's Resolve targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new HerosResolve()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Hero's Resolve attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new HerosResolve()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent aura = findPermanent(player1, "Hero's Resolve");
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+5")
    void enchantedCreatureGetsBuff() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HerosResolve());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(7);
    }

    @Test
    @DisplayName("Creature returns to base stats when Hero's Resolve is removed")
    void effectsStopWhenRemoved() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HerosResolve());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(7);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Hero's Resolve can enchant a creature an opponent controls")
    void canEnchantOpponentsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HerosResolve()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Hero's Resolve");
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(7);
    }

    @Test
    @DisplayName("Hero's Resolve does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HerosResolve());
        aura.setAttachedTo(enchanted.getId());

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Hero's Resolve fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new HerosResolve()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hero's Resolve");
        harness.assertNotOnBattlefield(player1, "Hero's Resolve");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Hero's Resolve")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new HerosResolve()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
