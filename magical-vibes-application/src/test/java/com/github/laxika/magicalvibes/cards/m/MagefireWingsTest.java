package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MagefireWingsTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Resolving Magefire Wings attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new MagefireWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Magefire Wings")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== +2/+0 boost =====

    @Test
    @DisplayName("Enchanted creature gets +2/+0")
    void enchantedCreatureGetsBoost() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent wingsPerm = new Permanent(new MagefireWings());
        wingsPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(wingsPerm);

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
    }

    // ===== Flying =====

    @Test
    @DisplayName("Enchanted creature has flying")
    void enchantedCreatureHasFlying() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent wingsPerm = new Permanent(new MagefireWings());
        wingsPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(wingsPerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();
    }

    // ===== Effects stop when removed =====

    @Test
    @DisplayName("Creature loses boost and flying when Magefire Wings is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent wingsPerm = new Permanent(new MagefireWings());
        wingsPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(wingsPerm);

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(wingsPerm);

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isFalse();
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a noncreature permanent with Magefire Wings")
    void cannotTargetNonCreature() {
        // A creature must exist for the Aura to be castable at all (CR 601.2c); otherwise it is
        // unplayable for lack of any legal target. With one present, picking the noncreature
        // exercises the targeting restriction itself.
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new MagefireWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Does not affect other creatures =====

    @Test
    @DisplayName("Magefire Wings does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent otherBears = new Permanent(new GrizzlyBears());
        otherBears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(otherBears);

        Permanent wingsPerm = new Permanent(new MagefireWings());
        wingsPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(wingsPerm);

        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FLYING)).isFalse();
    }
}
