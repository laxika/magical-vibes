package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.FalkenrathTorturer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChosenOfMarkov.class, FalkenrathTorturer.class})
class ChosenOfMarkovTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms into Markov's Servant when ability is activated")
    void transformsIntoMarkovsServant() {
        Permanent chosen = addCreatureReady(player1, new ChosenOfMarkov());

        // Add a Vampire to tap as cost
        addCreatureReady(player1, new FalkenrathTorturer());

        int chosenIdx = gd.playerBattlefields.get(player1.getId()).indexOf(chosen);
        harness.activateAbility(player1, chosenIdx, null, null);
        harness.passBothPriorities();

        assertThat(chosen.isTransformed()).isTrue();
        assertThat(chosen.getCard().getName()).isEqualTo("Markov's Servant");
        assertThat(gqs.getEffectivePower(gd, chosen)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, chosen)).isEqualTo(4);
    }

    @Test
    @DisplayName("Chosen of Markov taps itself and the Vampire taps as cost")
    void tapsItselfAndVampire() {
        Permanent chosen = addCreatureReady(player1, new ChosenOfMarkov());

        Permanent vampire = addCreatureReady(player1, new FalkenrathTorturer());

        int chosenIdx = gd.playerBattlefields.get(player1.getId()).indexOf(chosen);
        harness.activateAbility(player1, chosenIdx, null, null);
        harness.passBothPriorities();

        assertThat(chosen.isTapped()).isTrue();
        assertThat(vampire.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without an untapped Vampire")
    void cannotActivateWithoutVampire() {
        Permanent chosen = addCreatureReady(player1, new ChosenOfMarkov());

        int chosenIdx = gd.playerBattlefields.get(player1.getId()).indexOf(chosen);

        assertThatThrownBy(() -> harness.activateAbility(player1, chosenIdx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate if only Vampire is already tapped")
    void cannotActivateWithTappedVampire() {
        Permanent chosen = addCreatureReady(player1, new ChosenOfMarkov());

        Permanent vampire = addCreatureReady(player1, new FalkenrathTorturer());
        vampire.tap();

        int chosenIdx = gd.playerBattlefields.get(player1.getId()).indexOf(chosen);

        assertThatThrownBy(() -> harness.activateAbility(player1, chosenIdx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple untapped Vampires presents a choice")
    void multipleVampiresPresentChoice() {
        Permanent chosen = addCreatureReady(player1, new ChosenOfMarkov());

        addCreatureReady(player1, new FalkenrathTorturer());
        addCreatureReady(player1, new FalkenrathTorturer());

        int chosenIdx = gd.playerBattlefields.get(player1.getId()).indexOf(chosen);
        harness.activateAbility(player1, chosenIdx, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Choosing a Vampire from multiple completes the transform")
    void choosingVampireCompletesTransform() {
        Permanent chosen = addCreatureReady(player1, new ChosenOfMarkov());

        Permanent vamp1 = addCreatureReady(player1, new FalkenrathTorturer());
        Permanent vamp2 = addCreatureReady(player1, new FalkenrathTorturer());

        int chosenIdx = gd.playerBattlefields.get(player1.getId()).indexOf(chosen);
        harness.activateAbility(player1, chosenIdx, null, null);

        // Choose vamp1
        harness.handlePermanentChosen(player1, vamp1.getId());
        harness.passBothPriorities();

        assertThat(chosen.isTransformed()).isTrue();
        assertThat(chosen.getCard().getName()).isEqualTo("Markov's Servant");
        assertThat(vamp1.isTapped()).isTrue();
        assertThat(vamp2.isTapped()).isFalse();
    }

    @Test
    void canTapSummoningSickVampireAndPaysCostsBeforeResolution() {
        Permanent chosen = addCreatureReady(player1, new ChosenOfMarkov());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new FalkenrathTorturer());
        vampire.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(chosen.isTapped()).isTrue();
        assertThat(vampire.isTapped()).isTrue();
        assertThat(chosen.isTransformed()).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(chosen.isTransformed()).isTrue();
        assertThat(chosen.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWhileChosenIsSummoningSick() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new ChosenOfMarkov());
        chosen.setSummoningSick(true);
        Permanent vampire = addCreatureReady(player1, new FalkenrathTorturer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(chosen.isTapped()).isFalse();
        assertThat(vampire.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTapOpponentsVampireForCost() {
        Permanent chosen = addCreatureReady(player1, new ChosenOfMarkov());
        Permanent vampire = addCreatureReady(player2, new FalkenrathTorturer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(chosen.isTapped()).isFalse();
        assertThat(vampire.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTapNonVampireCreatureForCost() {
        addCreatureReady(player1, new ChosenOfMarkov());
        addCreatureReady(player1, new ChosenOfMarkov());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileChosenIsTapped() {
        Permanent chosen = addCreatureReady(player1, new ChosenOfMarkov());
        chosen.tap();
        Permanent vampire = addCreatureReady(player1, new FalkenrathTorturer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(vampire.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

}
