package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Oakenform.class, RuneclawBear.class, RodOfRuin.class})
class OakenformTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +3/+3")
    void enchantedCreatureGetsBoost() {
        Permanent bearsPerm = addCreatureReady(player1, new RuneclawBear());

        Permanent oakenformPerm = harness.addToBattlefieldAndReturn(player1, new Oakenform());
        oakenformPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(5);
    }

    @Test
    @DisplayName("Creature loses boost when Oakenform is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new RuneclawBear());

        Permanent oakenformPerm = harness.addToBattlefieldAndReturn(player1, new Oakenform());
        oakenformPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(oakenformPerm);

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target a creature with Oakenform")
    void canTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Oakenform()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Oakenform")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.addToBattlefield(player1, new RodOfRuin());
        harness.setHand(player1, List.of(new Oakenform()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        Permanent artifact = findPermanent(player1, "Rod of Ruin");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Oakenform does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = addCreatureReady(player1, new RuneclawBear());

        Permanent otherBears = addCreatureReady(player1, new RuneclawBear());

        Permanent oakenformPerm = harness.addToBattlefieldAndReturn(player1, new Oakenform());
        oakenformPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolves attached to an opponent's creature and boosts it")
    void enchantsOpponentsCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new Oakenform()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, bear.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Oakenform");
        assertThat(aura.getAttachedTo()).isEqualTo(bear.getId());
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
    }

    @Test
    @DisplayName("Multiple Oakenforms give cumulative boosts")
    void multipleCopiesStack() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Oakenform(), new Oakenform()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castEnchantment(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Oakenform")).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(8);
    }
}
