package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IvySeerTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a creature +X/+X for the number of selected green cards")
    void givesTargetCreaturePlusForSelectedGreenCards() {
        addReadySeer();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GiantGrowth greenCard = new GiantGrowth();
        harness.setHand(player1, List.of(greenCard, new FountainOfYouth()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice)
                        gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(greenCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(greenCard.getId()));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Allows revealing zero green cards")
    void allowsRevealingZeroGreenCards() {
        addReadySeer();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FountainOfYouth()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addReadySeer();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GiantGrowth greenCard = new GiantGrowth();
        harness.setHand(player1, List.of(greenCard));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(greenCard.getId()));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addReadySeer();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new GiantGrowth()));
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadySeer() {
        Permanent seer = harness.addToBattlefieldAndReturn(player1, new IvySeer());
        seer.setSummoningSick(false);
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
