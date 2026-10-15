package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlacialPlating.class, BorealDruid.class, SnowCoveredPlains.class})
class GlacialPlatingTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +3/+3 for each age counter on Glacial Plating")
    void enchantedCreatureGetsAgeScaledBoost() {
        Permanent creature = addCreatureReady(player1, new BorealDruid());
        Permanent plating = harness.addToBattlefieldAndReturn(player1, new GlacialPlating());
        plating.setAttachedTo(creature.getId());
        plating.setCounterCount(CounterType.AGE, 2);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
    }

    @Test
    @DisplayName("Casting Glacial Plating attaches it to a creature")
    void castingAttachesToCreature() {
        Permanent creature = addCreatureReady(player1, new BorealDruid());
        harness.setHand(player1, java.util.List.of(new GlacialPlating()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Glacial Plating").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Cumulative upkeep adds an age counter and can be paid with snow mana")
    void cumulativeUpkeepUsesSnowMana() {
        Permanent creature = addCreatureReady(player1, new BorealDruid());
        Permanent plating = harness.addToBattlefieldAndReturn(player1, new GlacialPlating());
        plating.setAttachedTo(creature.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(plating.getCounterCount(CounterType.AGE)).isEqualTo(1);

        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(plating);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cumulative upkeep charges one snow mana per age counter")
    void cumulativeUpkeepScalesWithAgeCounters() {
        Permanent creature = addCreatureReady(player1, new BorealDruid());
        Permanent plating = harness.addToBattlefieldAndReturn(player1, new GlacialPlating());
        plating.setAttachedTo(creature.getId());
        plating.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(plating.getCounterCount(CounterType.AGE)).isEqualTo(2);

        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(plating);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    @Test
    @DisplayName("Ordinary mana cannot pay Glacial Plating's snow upkeep")
    void ordinaryManaCannotPaySnowUpkeep() {
        Permanent creature = addCreatureReady(player1, new BorealDruid());
        Permanent plating = harness.addToBattlefieldAndReturn(player1, new GlacialPlating());
        plating.setAttachedTo(creature.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(plating);
        harness.assertInGraveyard(player1, "Glacial Plating");
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Glacial Plating")
    void decliningUpkeepSacrifices() {
        Permanent creature = addCreatureReady(player1, new BorealDruid());
        Permanent plating = harness.addToBattlefieldAndReturn(player1, new GlacialPlating());
        plating.setAttachedTo(creature.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(plating);
        harness.assertInGraveyard(player1, "Glacial Plating");
    }

    @Test
    @DisplayName("Glacial Plating cannot enchant a noncreature permanent")
    void cannotEnchantNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredPlains());
        harness.setHand(player1, java.util.List.of(new GlacialPlating()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Only age counters on the Aura determine its bonus")
    void bonusTracksAuraCountersRatherThanCreatureCounters() {
        Permanent creature = addCreatureReady(player1, new BorealDruid());
        Permanent otherCreature = addCreatureReady(player1, new BorealDruid());
        Permanent plating = harness.addToBattlefieldAndReturn(player1, new GlacialPlating());
        plating.setAttachedTo(creature.getId());
        creature.setCounterCount(CounterType.AGE, 4);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);

        plating.setCounterCount(CounterType.AGE, 2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(1);

        plating.setCounterCount(CounterType.AGE, 0);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's enchanted creature gets the bonus and loses it when upkeep is declined")
    void canEnchantOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new BorealDruid());
        harness.setHand(player1, java.util.List.of(new GlacialPlating()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent plating = findPermanent(player1, "Glacial Plating");
        assertThat(plating.getAttachedTo()).isEqualTo(creature.getId());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(plating.getCounterCount(CounterType.AGE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Glacial Plating");
        harness.assertOnBattlefield(player2, "Boreal Druid");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("One snow mana cannot pay upkeep for two age counters even with ordinary mana available")
    void insufficientSnowManaSacrificesWithoutPartialPayment() {
        Permanent creature = addCreatureReady(player1, new BorealDruid());
        Permanent plating = harness.addToBattlefieldAndReturn(player1, new GlacialPlating());
        plating.setAttachedTo(creature.getId());
        plating.setCounterCount(CounterType.AGE, 1);
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Glacial Plating");
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }
}
