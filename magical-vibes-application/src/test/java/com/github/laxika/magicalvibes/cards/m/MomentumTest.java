package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MomentumTest extends BaseCardTest {

    private Permanent addEnchantedBears() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent momentum = new Permanent(new Momentum());
        momentum.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(momentum);
        return bears;
    }

    @Test
    @DisplayName("Accepting the upkeep trigger adds a growth counter and boosts the enchanted creature")
    void upkeepAcceptedAddsCounterAndBoostsCreature() {
        Permanent bears = addEnchantedBears();
        Permanent momentum = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Momentum)
                .findFirst()
                .orElseThrow();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(momentum.getCounterCount(CounterType.GROWTH)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves Momentum without a growth counter")
    void upkeepDeclinedAddsNoCounter() {
        addEnchantedBears();
        Permanent momentum = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Momentum)
                .findFirst()
                .orElseThrow();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(momentum.getCounterCount(CounterType.GROWTH)).isZero();
    }

    @Test
    @DisplayName("The enchanted creature gets one +1/+1 for each growth counter on Momentum")
    void boostScalesWithGrowthCounters() {
        Permanent bears = addEnchantedBears();
        Permanent momentum = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Momentum)
                .findFirst()
                .orElseThrow();
        momentum.setCounterCount(CounterType.GROWTH, 2);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Momentum can target a creature")
    void targetingCreaturePutsSpellOnStack() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Momentum()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Momentum cannot target a noncreature permanent")
    void targetingRequiresCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Momentum()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof FountainOfYouth)
                .findFirst()
                .orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
