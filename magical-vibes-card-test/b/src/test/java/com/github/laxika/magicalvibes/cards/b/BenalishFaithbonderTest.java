package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AutomaticLibrarian;
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

@CardUsed({BenalishFaithbonder.class, AutomaticLibrarian.class, ColossalGrowth.class})
class BenalishFaithbonderTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance keeps Benalish Faithbonder untapped when it attacks")
    void vigilanceKeepsItUntappedWhenItAttacks() {
        Permanent faithbonder = addCreatureReady(player1, new BenalishFaithbonder());

        declareAttackers(List.of(0));

        assertThat(faithbonder.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enlist taps a nonattacking creature and gives Benalish Faithbonder its power")
    void enlistBoostsAttackerBySupporterPower() {
        Permanent faithbonder = addCreatureReady(player1, new BenalishFaithbonder());
        Permanent supporter = addCreatureReady(player1, new AutomaticLibrarian());

        declareAttackers(List.of(0));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(supporter.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        assertThat(supporter.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(faithbonder.getPowerModifier()).isEqualTo(3);
        assertThat(faithbonder.getToughnessModifier()).isZero();
    }

    @Test
    void enlistUsesSupportersPowerWhenTriggerResolves() {
        Permanent faithbonder = addCreatureReady(player1, new BenalishFaithbonder());
        Permanent supporter = addCreatureReady(player1, new AutomaticLibrarian());
        harness.setHand(player1, List.of(new ColossalGrowth()));

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        assertThat(faithbonder.getPowerModifier()).isZero();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, supporter.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, supporter)).isEqualTo(6);

        harness.passBothPriorities();

        assertThat(faithbonder.getPowerModifier()).isEqualTo(6);
        assertThat(faithbonder.getToughnessModifier()).isZero();
    }

    @Test
    void enlistCanBeDeclined() {
        Permanent faithbonder = addCreatureReady(player1, new BenalishFaithbonder());
        Permanent supporter = addCreatureReady(player1, new AutomaticLibrarian());

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(supporter.isTapped()).isFalse();
        assertThat(faithbonder.isTapped()).isFalse();
        assertThat(faithbonder.getPowerModifier()).isZero();
    }

    @Test
    void enlistExcludesTappedSummoningSickOpposingAndAttackingCreatures() {
        addCreatureReady(player1, new BenalishFaithbonder());
        Permanent otherAttacker = addCreatureReady(player1, new BenalishFaithbonder());
        Permanent tapped = addCreatureReady(player1, new AutomaticLibrarian());
        tapped.tap();
        Permanent summoningSick = harness.addToBattlefieldAndReturn(player1, new AutomaticLibrarian());
        summoningSick.setSummoningSick(true);
        addCreatureReady(player2, new AutomaticLibrarian());
        Permanent eligible = addCreatureReady(player1, new AutomaticLibrarian());

        declareAttackers(List.of(0, 1));

        assertThat(otherAttacker.isTapped()).isFalse();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(eligible.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(eligible.getId()));
        resolveAllTriggers();

        assertThat(eligible.isTapped()).isTrue();
        assertThat(tapped.isTapped()).isTrue();
        assertThat(summoningSick.isTapped()).isFalse();
        assertThat(otherAttacker.getPowerModifier()).isZero();
    }

    @Test
    void enlistBonusExpiresAtEndOfTurn() {
        Permanent faithbonder = addCreatureReady(player1, new BenalishFaithbonder());
        Permanent supporter = addCreatureReady(player1, new AutomaticLibrarian());

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        resolveAllTriggers();
        assertThat(faithbonder.getPowerModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(faithbonder.getPowerModifier()).isZero();
        assertThat(faithbonder.getToughnessModifier()).isZero();
    }
}
