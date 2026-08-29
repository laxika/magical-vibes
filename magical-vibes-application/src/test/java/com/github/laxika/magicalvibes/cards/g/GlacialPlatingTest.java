package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GlacialPlatingTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +3/+3 for each age counter on Glacial Plating")
    void enchantedCreatureGetsAgeScaledBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent plating = harness.addToBattlefieldAndReturn(player1, new GlacialPlating());
        plating.setAttachedTo(creature.getId());
        plating.setCounterCount(CounterType.AGE, 2);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(8);
    }

    @Test
    @DisplayName("Cumulative upkeep adds an age counter and can be paid with snow mana")
    void cumulativeUpkeepUsesSnowMana() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
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
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Glacial Plating")
    void decliningUpkeepSacrifices() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
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
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, java.util.List.of(new GlacialPlating()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
