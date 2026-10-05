package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagefireWings.class, GrizzlyBears.class, FountainOfYouth.class})
class MagefireWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Magefire Wings attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new MagefireWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Magefire Wings")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+0")
    void enchantedCreatureGetsBoost() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent wingsPerm = harness.addToBattlefieldAndReturn(player1, new MagefireWings());
        wingsPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enchanted creature has flying")
    void enchantedCreatureHasFlying() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent wingsPerm = harness.addToBattlefieldAndReturn(player1, new MagefireWings());
        wingsPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Creature loses boost and flying when Magefire Wings is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent wingsPerm = harness.addToBattlefieldAndReturn(player1, new MagefireWings());
        wingsPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(wingsPerm);

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Magefire Wings")
    void cannotTargetNonCreature() {
        // A creature must exist for the Aura to be castable at all (CR 601.2c); otherwise it is
        // unplayable for lack of any legal target. With one present, picking the noncreature
        // exercises the targeting restriction itself.
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new MagefireWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Magefire Wings does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent otherBears = addCreatureReady(player1, new GrizzlyBears());

        Permanent wingsPerm = harness.addToBattlefieldAndReturn(player1, new MagefireWings());
        wingsPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Magefire Wings can enchant an opponent's creature")
    void enchantsOpponentsCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MagefireWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Magefire Wings").getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
    }

    @Test
    @DisplayName("Multiple Magefire Wings stack their boosts and retain flying when one leaves")
    void multipleCopiesStack() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MagefireWings());
        first.setAttachedTo(bears.getId());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MagefireWings());
        second.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Magefire Wings goes to the graveyard if its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MagefireWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerGraveyards.get(player1.getId()).add(bears.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof MagefireWings);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c instanceof MagefireWings);
    }
}
