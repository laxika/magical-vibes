package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OakenformTest extends BaseCardTest {

    // ===== +3/+3 boost =====

    @Test
    @DisplayName("Enchanted creature gets +3/+3")
    void enchantedCreatureGetsBoost() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent oakenformPerm = new Permanent(new Oakenform());
        oakenformPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(oakenformPerm);

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(5);
    }

    // ===== Effects stop when removed =====

    @Test
    @DisplayName("Creature loses boost when Oakenform is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent oakenformPerm = new Permanent(new Oakenform());
        oakenformPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(oakenformPerm);

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(oakenformPerm);

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Can target a creature with Oakenform")
    void canTargetCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.setHand(player1, List.of(new Oakenform()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Oakenform")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Oakenform()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Does not affect other creatures =====

    @Test
    @DisplayName("Oakenform does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent otherBears = new Permanent(new GrizzlyBears());
        otherBears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(otherBears);

        Permanent oakenformPerm = new Permanent(new Oakenform());
        oakenformPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(oakenformPerm);

        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(2);
    }
}
