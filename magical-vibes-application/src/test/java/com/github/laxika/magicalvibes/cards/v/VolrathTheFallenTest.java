package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VolrathTheFallenTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a creature card boosts Volrath by its mana value")
    void discardCreatureBoostsByManaValue() {
        Permanent volrath = addReadyVolrath(player1);
        int basePower = gqs.getEffectivePower(gd, volrath);
        int baseToughness = gqs.getEffectiveToughness(gd, volrath);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addActivationMana();

        harness.activateAbility(player1, 0, 0, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, volrath)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, volrath)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Only creature cards are valid for the discard cost")
    void onlyCreatureCardsAreValid() {
        addReadyVolrath(player1);
        harness.setHand(player1, List.of(new LightningBolt(), new GrizzlyBears()));
        addActivationMana();

        harness.activateAbility(player1, 0, 0, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Cannot activate without a creature card to discard")
    void cannotActivateWithoutCreatureCard() {
        addReadyVolrath(player1);
        harness.setHand(player1, List.of(new LightningBolt()));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent volrath = addReadyVolrath(player1);
        int basePower = gqs.getEffectivePower(gd, volrath);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addActivationMana();

        harness.activateAbility(player1, 0, 0, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, volrath)).isEqualTo(basePower + 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, volrath)).isEqualTo(basePower);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addReadyVolrath(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent volrath = harness.addToBattlefieldAndReturn(player, new VolrathTheFallen());
        volrath.setSummoningSick(false);
        return volrath;
    }
}
