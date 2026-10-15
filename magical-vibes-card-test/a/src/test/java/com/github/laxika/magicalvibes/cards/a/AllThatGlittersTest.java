package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoldenEgg;
import com.github.laxika.magicalvibes.cards.g.GarenbrigSquire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AllThatGlitters.class, GoldenEgg.class, GarenbrigSquire.class})
class AllThatGlittersTest extends BaseCardTest {

    @Test
    @DisplayName("All That Glitters grants +1/+1 for each artifact or enchantment its controller controls")
    void scalesWithControlledArtifactsAndEnchantments() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GarenbrigSquire());
        harness.addToBattlefield(player1, new GoldenEgg());

        harness.setHand(player1, List.of(new AllThatGlitters()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        harness.addToBattlefield(player1, new GoldenEgg());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("All That Glitters counts only artifacts and enchantments controlled by its controller")
    void ignoresOpponentsArtifacts() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GarenbrigSquire());
        harness.addToBattlefield(player2, new GoldenEgg());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AllThatGlitters());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("All That Glitters stops boosting when it leaves the battlefield")
    void effectEndsWhenAuraLeavesBattlefield() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GarenbrigSquire());
        harness.addToBattlefield(player1, new GoldenEgg());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AllThatGlitters());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("All That Glitters cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GoldenEgg());
        harness.setHand(player1, List.of(new AllThatGlitters()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent artifact = findPermanent(player1, "Golden Egg");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void enchantingOpponentsCreatureCountsAuraControllersPermanents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GarenbrigSquire());
        harness.addToBattlefield(player1, new GoldenEgg());
        harness.addToBattlefield(player2, new GoldenEgg());
        harness.addToBattlefield(player2, new GoldenEgg());
        harness.setHand(player1, List.of(new AllThatGlitters()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void multipleAurasCountEachOtherAndBoostOnlyTheirAttachedCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GarenbrigSquire());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GarenbrigSquire());
        Permanent firstAura = harness.addToBattlefieldAndReturn(player1, new AllThatGlitters());
        firstAura.setAttachedTo(first.getId());
        Permanent secondAura = harness.addToBattlefieldAndReturn(player1, new AllThatGlitters());
        secondAura.setAttachedTo(second.getId());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(secondAura);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    void sacrificingArtifactImmediatelyReducesBonus() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GarenbrigSquire());
        harness.addToBattlefield(player1, new GoldenEgg());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AllThatGlitters());
        aura.setAttachedTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }
}
