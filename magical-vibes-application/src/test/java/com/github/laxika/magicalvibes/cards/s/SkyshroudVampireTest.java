package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FightingDrake;
import com.github.laxika.magicalvibes.cards.l.LightningBlast;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyshroudVampire.class, FightingDrake.class, LightningBlast.class})
class SkyshroudVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a creature card gives Skyshroud Vampire +2/+2")
    void discardCreatureBoosts() {
        Permanent vampire = addCreatureReady(player1, new SkyshroudVampire());
        harness.setHand(player1, List.of(new FightingDrake()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fighting Drake");
        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(5);
    }

    @Test
    @DisplayName("Only creature cards are valid for the discard cost")
    void onlyCreatureCardsAreValid() {
        addCreatureReady(player1, new SkyshroudVampire());
        harness.setHand(player1, List.of(new LightningBlast(), new FightingDrake()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Cannot activate without a creature card in hand")
    void cannotActivateWithoutCreatureCard() {
        addCreatureReady(player1, new SkyshroudVampire());
        harness.setHand(player1, List.of(new LightningBlast()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        Permanent vampire = addCreatureReady(player1, new SkyshroudVampire());
        harness.setHand(player1, List.of(new FightingDrake()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability can be activated multiple times and the boosts stack")
    void boostsStack() {
        Permanent vampire = addCreatureReady(player1, new SkyshroudVampire());
        harness.setHand(player1, List.of(new FightingDrake(), new FightingDrake()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(7);
    }

    @Test
    @DisplayName("The ability can be activated while Skyshroud Vampire is tapped")
    void abilityDoesNotRequireTapping() {
        Permanent vampire = addCreatureReady(player1, new SkyshroudVampire());
        vampire.tap();
        harness.setHand(player1, List.of(new FightingDrake()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(vampire.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(5);
    }
}
