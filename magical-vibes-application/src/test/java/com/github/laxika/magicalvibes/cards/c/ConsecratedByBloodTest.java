package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConsecratedByBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2 and has flying")
    void enchantedCreatureGetsBoostAndFlying() {
        Permanent bears = enchantedBears();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Boost and flying go away when the Aura leaves")
    void effectsStopWhenAuraLeaves() {
        Permanent bears = enchantedBears();
        Permanent aura = findPermanent(player1, "Consecrated by Blood");

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Sacrificing two other creatures regenerates the enchanted creature")
    void sacrificingTwoOtherCreaturesRegenerates() {
        Permanent bears = enchantedBears();
        UUID fodder1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID fodder2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.activateAbility(player1, 0, null, null);
        // Picking the first sacrifice leaves a single legal choice for the second, which auto-pays.
        harness.handlePermanentChosen(player1, fodder1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(fodder1) || p.getId().equals(fodder2));
        assertThat(bears.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The granted ability can't be activated with only one other creature")
    void requiresTwoOtherCreatures() {
        enchantedBears();
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("The enchanted creature itself can't pay for its own regeneration")
    void enchantedCreatureCannotSacrificeItself() {
        Permanent bears = enchantedBears();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Consecrated by Blood can't enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ConsecratedByBlood()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    /** Puts a ready Grizzly Bears on player1's battlefield enchanted by a resolved Consecrated by Blood. */
    private Permanent enchantedBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        harness.setHand(player1, List.of(new ConsecratedByBlood()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        return bears;
    }
}
