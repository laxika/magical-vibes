package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ColossalGrowth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarkweaveCrusher.class, GrizzlyBears.class, ColossalGrowth.class})
class BarkweaveCrusherTest extends BaseCardTest {

    @Test
    void enlistCanBeDeclined() {
        Permanent crusher = addCreatureReady(player1, new BarkweaveCrusher());
        Permanent supporter = addCreatureReady(player1, new BarkweaveCrusher());

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(supporter.isTapped()).isFalse();
        assertThat(crusher.getPowerModifier()).isZero();
    }

    @Test
    void enlistExcludesTappedSummoningSickAttackingAndOpposingCreatures() {
        addCreatureReady(player1, new BarkweaveCrusher());
        Permanent tapped = addCreatureReady(player1, new BarkweaveCrusher());
        tapped.tap();
        Permanent sick = addCreatureReady(player1, new BarkweaveCrusher());
        sick.setSummoningSick(true);
        addCreatureReady(player1, new BarkweaveCrusher());
        Permanent eligible = addCreatureReady(player1, new BarkweaveCrusher());
        addCreatureReady(player2, new BarkweaveCrusher());

        declareAttackers(List.of(0, 3));
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(eligible.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        assertThat(eligible.isTapped()).isTrue();
        assertThat(sick.isTapped()).isFalse();
    }

    @Test
    void enlistUsesSupporterPowerWhenTriggerResolves() {
        Permanent crusher = addCreatureReady(player1, new BarkweaveCrusher());
        Permanent supporter = addCreatureReady(player1, new BarkweaveCrusher());
        harness.setHand(player1, List.of(new ColossalGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
            assertThat(crusher.getPowerModifier()).isZero();
            assertThat(gd.stack).hasSize(1);
            harness.castInstant(player1, 0, supporter.getId());
            harness.passBothPriorities();
            assertThat(supporter.getPowerModifier()).isEqualTo(3);
            harness.passBothPriorities();
        });

        assertThat(crusher.getPowerModifier()).isEqualTo(5);
        assertThat(crusher.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Enlist taps a nonattacking creature and boosts Barkweave Crusher by its power")
    void enlistBoostsAttackerBySupporterPower() {
        Permanent crusher = addCreatureReady(player1, new BarkweaveCrusher());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(supporter.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.passBothPriorities();

        assertThat(supporter.isTapped()).isTrue();
        assertThat(crusher.getPowerModifier()).isEqualTo(2);
        assertThat(crusher.getToughnessModifier()).isZero();
    }
}
