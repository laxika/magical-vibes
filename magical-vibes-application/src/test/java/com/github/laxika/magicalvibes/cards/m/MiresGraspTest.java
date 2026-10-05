package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MiresGrasp.class, FountainOfYouth.class, GrizzlyBears.class, NyxbornColossus.class})
class MiresGraspTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets -3/-3")
    void enchantedCreatureGetsDebuff() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MiresGrasp());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(-1);
    }

    @Test
    @DisplayName("Creature returns to base stats when Mire's Grasp leaves")
    void effectsStopWhenRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MiresGrasp());
        aura.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mire's Grasp fizzles if its target creature leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new MiresGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mire's Grasp");
        harness.assertNotOnBattlefield(player1, "Mire's Grasp");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Mire's Grasp")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new MiresGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Resolves attached to an opponent's creature and affects only that creature")
    void resolvesOnOpposingCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        harness.setHand(player1, List.of(new MiresGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mire's Grasp");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(aura -> assertThat(aura.getAttachedTo()).isEqualTo(creature.getId()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(7);
    }

    @Test
    @DisplayName("Lethal toughness reduction puts the creature and its Aura into their owners' graveyards")
    void lethalDebuffRemovesCreatureAndAura() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MiresGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Mire's Grasp");
        harness.assertNotOnBattlefield(player1, "Mire's Grasp");
    }

    @Test
    @DisplayName("Multiple copies apply cumulative debuffs")
    void multipleCopiesStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        harness.setHand(player1, List.of(new MiresGrasp(), new MiresGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Nyxborn Colossus");
        assertThat(gqs.getEffectivePower(gd, creature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }
}
