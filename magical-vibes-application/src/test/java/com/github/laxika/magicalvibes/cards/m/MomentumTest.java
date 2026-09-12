package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AncientSilverback;
import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Momentum.class, AncientSilverback.class, BraidwoodCup.class})
class MomentumTest extends BaseCardTest {

    private Permanent addEnchantedSilverback() {
        Permanent silverback = addCreatureReady(player1, new AncientSilverback());
        Permanent momentum = harness.addToBattlefieldAndReturn(player1, new Momentum());
        momentum.setAttachedTo(silverback.getId());
        return silverback;
    }

    @Test
    @DisplayName("Accepting the upkeep trigger adds a growth counter and boosts the enchanted creature")
    void upkeepAcceptedAddsCounterAndBoostsCreature() {
        Permanent silverback = addEnchantedSilverback();
        Permanent momentum = findPermanent(player1, "Momentum");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(momentum.getCounterCount(CounterType.GROWTH)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, silverback)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, silverback)).isEqualTo(6);
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves Momentum without a growth counter")
    void upkeepDeclinedAddsNoCounter() {
        addEnchantedSilverback();
        Permanent momentum = findPermanent(player1, "Momentum");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(momentum.getCounterCount(CounterType.GROWTH)).isZero();
    }

    @Test
    @DisplayName("The enchanted creature gets one +1/+1 for each growth counter on Momentum")
    void boostScalesWithGrowthCounters() {
        Permanent silverback = addEnchantedSilverback();
        Permanent momentum = findPermanent(player1, "Momentum");
        momentum.setCounterCount(CounterType.GROWTH, 2);

        assertThat(gqs.getEffectivePower(gd, silverback)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, silverback)).isEqualTo(7);
    }

    @Test
    @DisplayName("Momentum can target a creature")
    void targetingCreaturePutsSpellOnStack() {
        Permanent creature = addCreatureReady(player1, new AncientSilverback());
        harness.setHand(player1, List.of(new Momentum()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Momentum can enchant a creature controlled by an opponent")
    void canEnchantOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new AncientSilverback());
        harness.setHand(player1, List.of(new Momentum()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Momentum").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Resolving Momentum attaches it to the targeted creature")
    void resolvingAttachesToTargetCreature() {
        Permanent creature = addCreatureReady(player1, new AncientSilverback());
        harness.setHand(player1, List.of(new Momentum()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent momentum = findPermanent(player1, "Momentum");
        assertThat(momentum.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Momentum's upkeep trigger does not occur during an opponent's upkeep")
    void upkeepTriggerOnlyOccursDuringControllersUpkeep() {
        addEnchantedSilverback();
        Permanent momentum = findPermanent(player1, "Momentum");

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(momentum.getCounterCount(CounterType.GROWTH)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Momentum cannot target a noncreature permanent")
    void targetingRequiresCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BraidwoodCup());
        harness.setHand(player1, List.of(new Momentum()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
