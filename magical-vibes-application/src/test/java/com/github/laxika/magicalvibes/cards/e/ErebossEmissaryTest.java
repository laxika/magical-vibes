package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ErebossEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a creature card gives the creature +2/+2")
    void creatureAbilityBoostsSelf() {
        Permanent emissary = harness.addToBattlefieldAndReturn(player1, new ErebossEmissary());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, emissary)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, emissary)).isEqualTo(5);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ability only allows a creature card to be discarded")
    void abilityRequiresCreatureCard() {
        harness.addToBattlefield(player1, new ErebossEmissary());
        harness.setHand(player1, List.of(new Mountain()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a creature card");
    }

    @Test
    @DisplayName("A bestowed Emissary boosts its enchanted creature with both abilities")
    void bestowedAbilityBoostsEnchantedCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setSummoningSick(false);
        harness.setHand(player1, List.of(new ErebossEmissary(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(7);
    }

    @Test
    @DisplayName("The temporary boost wears off at end of turn while the Aura boost remains")
    void bestowedTemporaryBoostWearsOff() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setSummoningSick(false);
        harness.setHand(player1, List.of(new ErebossEmissary(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);
    }
}
