package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NaturalEnd;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiritAway.class, GrizzlyBears.class, FountainOfYouth.class, NaturalEnd.class, Cloudshift.class})
class SpiritAwayTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Spirit Away steals the enchanted creature and pumps it")
    void resolvingStealsAndPumpsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        int basePower = gqs.getEffectivePower(gd, creature);
        int baseToughness = gqs.getEffectiveToughness(gd, creature);

        harness.setHand(player1, List.of(new SpiritAway()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(baseToughness + 2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Removing the Aura takes away the boost and flying")
    void removingAuraRevertsBoost() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        int basePower = gqs.getEffectivePower(gd, creature);

        harness.setHand(player1, List.of(new SpiritAway()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Spirit Away");
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(basePower);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Spirit Away")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SpiritAway()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Destroying Spirit Away restores control and removes both stat bonuses and flying")
    void destroyingAuraRestoresControlAndRemovesBonuses() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        int basePower = gqs.getEffectivePower(gd, creature);
        int baseToughness = gqs.getEffectiveToughness(gd, creature);
        harness.setHand(player1, List.of(new SpiritAway()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        Permanent aura = findPermanent(player1, "Spirit Away");
        harness.setHand(player2, List.of(new NaturalEnd()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spirit Away");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Spirit Away can enchant your own creature without changing control")
    void enchantingOwnCreatureDoesNotCauseSummoningSickness() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        int basePower = gqs.getEffectivePower(gd, creature);
        int baseToughness = gqs.getEffectiveToughness(gd, creature);
        harness.setHand(player1, List.of(new SpiritAway()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(creature.isSummoningSick()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(baseToughness + 2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Blinking the target in response makes Spirit Away fail to resolve")
    void blinkedTargetIsNotStolenOrBoosted() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        int basePower = gqs.getEffectivePower(gd, creature);
        int baseToughness = gqs.getEffectiveToughness(gd, creature);
        harness.setHand(player1, List.of(new SpiritAway()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castEnchantment(player1, 0, creature.getId());

        harness.setHand(player2, List.of(new Cloudshift()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Spirit Away");
        harness.assertNotOnBattlefield(player1, "Spirit Away");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        Permanent returned = findPermanent(player2, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The newest Spirit Away controls the creature while both Auras grant their bonuses")
    void removingNewerAuraRestoresOlderAuraControl() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        int basePower = gqs.getEffectivePower(gd, creature);
        int baseToughness = gqs.getEffectiveToughness(gd, creature);
        harness.setHand(player1, List.of(new SpiritAway()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SpiritAway()));
        harness.addMana(player2, ManaColor.BLUE, 7);
        harness.castEnchantment(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(basePower + 4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(baseToughness + 4);

        Permanent newerAura = findPermanent(player2, "Spirit Away");
        harness.setHand(player1, List.of(new NaturalEnd()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, newerAura.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Spirit Away");
        harness.assertOnBattlefield(player1, "Spirit Away");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(baseToughness + 2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }
}
