package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.b.BaronyVampire;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChosenOfMarkovTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms into Markov's Servant when ability is activated")
    void transformsIntoMarkovsServant() {
        harness.addToBattlefield(player1, new ChosenOfMarkov());
        Permanent chosen = findPermanent(player1, "Chosen of Markov");
        chosen.setSummoningSick(false);

        // Add a Vampire to tap as cost
        Permanent vampire = addCreatureReady(player1, new BaronyVampire());

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
        harness.addToBattlefield(player1, new ChosenOfMarkov());
        Permanent chosen = findPermanent(player1, "Chosen of Markov");
        chosen.setSummoningSick(false);

        Permanent vampire = addCreatureReady(player1, new BaronyVampire());

        int chosenIdx = gd.playerBattlefields.get(player1.getId()).indexOf(chosen);
        harness.activateAbility(player1, chosenIdx, null, null);
        harness.passBothPriorities();

        assertThat(chosen.isTapped()).isTrue();
        assertThat(vampire.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without an untapped Vampire")
    void cannotActivateWithoutVampire() {
        harness.addToBattlefield(player1, new ChosenOfMarkov());
        Permanent chosen = findPermanent(player1, "Chosen of Markov");
        chosen.setSummoningSick(false);

        int chosenIdx = gd.playerBattlefields.get(player1.getId()).indexOf(chosen);

        assertThatThrownBy(() -> harness.activateAbility(player1, chosenIdx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate if only Vampire is already tapped")
    void cannotActivateWithTappedVampire() {
        harness.addToBattlefield(player1, new ChosenOfMarkov());
        Permanent chosen = findPermanent(player1, "Chosen of Markov");
        chosen.setSummoningSick(false);

        Permanent vampire = addCreatureReady(player1, new BaronyVampire());
        vampire.tap();

        int chosenIdx = gd.playerBattlefields.get(player1.getId()).indexOf(chosen);

        assertThatThrownBy(() -> harness.activateAbility(player1, chosenIdx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple untapped Vampires presents a choice")
    void multipleVampiresPresentChoice() {
        harness.addToBattlefield(player1, new ChosenOfMarkov());
        Permanent chosen = findPermanent(player1, "Chosen of Markov");
        chosen.setSummoningSick(false);

        addCreatureReady(player1, new BaronyVampire());
        addCreatureReady(player1, new BaronyVampire());

        int chosenIdx = gd.playerBattlefields.get(player1.getId()).indexOf(chosen);
        harness.activateAbility(player1, chosenIdx, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Choosing a Vampire from multiple completes the transform")
    void choosingVampireCompletesTransform() {
        harness.addToBattlefield(player1, new ChosenOfMarkov());
        Permanent chosen = findPermanent(player1, "Chosen of Markov");
        chosen.setSummoningSick(false);

        Permanent vamp1 = addCreatureReady(player1, new BaronyVampire());
        Permanent vamp2 = addCreatureReady(player1, new BaronyVampire());

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

    
}
