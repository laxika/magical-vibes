package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VampireAristocrat;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StromkirkCondemnedTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card boosts only Vampires you control")
    void boostsOwnVampires() {
        Permanent condemned = addCreatureReady(player1, new StromkirkCondemned());
        Permanent ownVampire = addCreatureReady(player1, new VampireAristocrat());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentVampire = addCreatureReady(player2, new VampireAristocrat());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(condemned.getEffectivePower()).isEqualTo(3);
        assertThat(condemned.getEffectiveToughness()).isEqualTo(3);
        assertThat(ownVampire.getEffectivePower()).isEqualTo(3);
        assertThat(ownVampire.getEffectiveToughness()).isEqualTo(3);
        assertThat(ownBear.getEffectivePower()).isEqualTo(2);
        assertThat(ownBear.getEffectiveToughness()).isEqualTo(2);
        assertThat(opponentVampire.getEffectivePower()).isEqualTo(2);
        assertThat(opponentVampire.getEffectiveToughness()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ability can be activated only once each turn")
    void onlyOncePerTurn() {
        addCreatureReady(player1, new StromkirkCondemned());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent condemned = addCreatureReady(player1, new StromkirkCondemned());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(condemned.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(condemned.getEffectivePower()).isEqualTo(2);
        assertThat(condemned.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability cannot be activated without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new StromkirkCondemned());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card");
    }

}
