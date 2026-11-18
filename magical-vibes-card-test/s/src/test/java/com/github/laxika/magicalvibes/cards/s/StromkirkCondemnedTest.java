package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VampireAristocrat;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StromkirkCondemned.class, GrizzlyBears.class, VampireAristocrat.class})
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

        assertThat(gqs.getEffectivePower(gd, condemned)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, condemned)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownVampire)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownVampire)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentVampire)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentVampire)).isEqualTo(2);
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
        assertThat(gqs.getEffectivePower(gd, condemned)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, condemned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, condemned)).isEqualTo(2);
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

    @Test
    @DisplayName("Discard is paid before resolution and the activation limit applies while on the stack")
    void paysCostAndConsumesActivationBeforeResolution() {
        Permanent condemned = harness.addToBattlefieldAndReturn(player1, new StromkirkCondemned());
        harness.setHand(player1, List.of(new StromkirkCondemned(), new StromkirkCondemned()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Stromkirk Condemned");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, condemned)).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, condemned)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, condemned)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost affects Vampires present at resolution, but not those entering afterward")
    void determinesAffectedVampiresAtResolution() {
        Permanent condemned = addCreatureReady(player1, new StromkirkCondemned());
        harness.setHand(player1, List.of(new StromkirkCondemned()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new StromkirkCondemned());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new StromkirkCondemned());

        assertThat(gqs.getEffectivePower(gd, condemned)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, condemned)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each copy can activate once and their boosts stack")
    void separateCopiesHaveSeparateActivationLimits() {
        Permanent first = addCreatureReady(player1, new StromkirkCondemned());
        Permanent second = addCreatureReady(player1, new StromkirkCondemned());
        harness.setHand(player1, List.of(new StromkirkCondemned(), new StromkirkCondemned()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("The activation limit resets on the opponent's turn and the ability works then")
    void canActivateAgainOnOpponentsTurn() {
        Permanent condemned = addCreatureReady(player1, new StromkirkCondemned());
        harness.setHand(player1, List.of(new StromkirkCondemned(), new StromkirkCondemned()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, condemned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, condemned)).isEqualTo(2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, condemned)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, condemned)).isEqualTo(3);
    }

}
