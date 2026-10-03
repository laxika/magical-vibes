package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Atogatog.class, Atog.class, GrizzlyBears.class})
class AtogatogTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an Atog gives Atogatog +X/+X based on its power")
    void sacrificeBoostsBySacrificedPower() {
        Permanent atogatog = harness.addToBattlefieldAndReturn(player1, new Atogatog());
        Permanent atog = harness.addToBattlefieldAndReturn(player1, new Atog());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, atog.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Atog");
        assertThat(atogatog.getPowerModifier()).isEqualTo(1);
        assertThat(atogatog.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Uses the sacrificed Atog's effective power")
    void usesEffectivePower() {
        Permanent atogatog = harness.addToBattlefieldAndReturn(player1, new Atogatog());
        Permanent atog = addCreatureReady(player1, new Atog());
        atog.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, atog.getId());
        harness.passBothPriorities();

        assertThat(atogatog.getPowerModifier()).isEqualTo(2);
        assertThat(atogatog.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can sacrifice Atogatog itself")
    void canSacrificeItself() {
        harness.addToBattlefield(player1, new Atogatog());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Atogatog");
    }

    @Test
    @DisplayName("Rejects a non-Atog creature as the sacrifice choice")
    void cannotSacrificeNonAtogCreature() {
        harness.addToBattlefield(player1, new Atogatog());
        Permanent grizzlyBears = addCreatureReady(player1, new GrizzlyBears());

        addCreatureReady(player1, new Atog());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, grizzlyBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Atogatog"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Atogatog");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only Atog creatures can be chosen for the sacrifice")
    void onlyAtogsCanBeChosen() {
        harness.addToBattlefield(player1, new Atogatog());
        Permanent atog = addCreatureReady(player1, new Atog());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, atog.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Atog");
    }

    @Test
    @DisplayName("The power and toughness boost expires at the end of the turn")
    void boostExpiresAtEndOfTurn() {
        Permanent atogatog = harness.addToBattlefieldAndReturn(player1, new Atogatog());
        Permanent atog = addCreatureReady(player1, new Atog());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, atog.getId());
        harness.passBothPriorities();

        assertThat(atogatog.getPowerModifier()).isEqualTo(1);
        assertThat(atogatog.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(atogatog.getPowerModifier()).isZero();
        assertThat(atogatog.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Sacrificing a zero-power Atog gives no boost")
    void zeroPowerGivesNoBoost() {
        Permanent atogatog = harness.addToBattlefieldAndReturn(player1, new Atogatog());
        Permanent atog = harness.addToBattlefieldAndReturn(player1, new Atog());
        atog.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, atog.getId());
        harness.assertInGraveyard(player1, "Atog");
        harness.passBothPriorities();

        assertThat(atogatog.getPowerModifier()).isZero();
        assertThat(atogatog.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Sacrificing a negative-power Atog gives no boost or penalty")
    void negativePowerGivesNoBoostOrPenalty() {
        Permanent atogatog = harness.addToBattlefieldAndReturn(player1, new Atogatog());
        Permanent atog = harness.addToBattlefieldAndReturn(player1, new Atog());
        atog.setPowerModifier(-2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, atog.getId());
        harness.assertInGraveyard(player1, "Atog");
        harness.passBothPriorities();

        assertThat(atogatog.getPowerModifier()).isZero();
        assertThat(atogatog.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent's Atog cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsAtog() {
        Permanent atogatog = harness.addToBattlefieldAndReturn(player1, new Atogatog());
        Permanent ownAtog = harness.addToBattlefieldAndReturn(player1, new Atog());
        Permanent opponentsAtog = harness.addToBattlefieldAndReturn(player2, new Atog());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentsAtog.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, ownAtog.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Atog");
        harness.assertNotInGraveyard(player2, "Atog");
        harness.assertInGraveyard(player1, "Atog");
        assertThat(atogatog.getPowerModifier()).isEqualTo(1);
        assertThat(atogatog.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each activation keeps the power of its own sacrificed Atog")
    void separateActivationsKeepTheirOwnSacrificedPower() {
        Permanent atogatog = harness.addToBattlefieldAndReturn(player1, new Atogatog());
        Permanent firstAtog = harness.addToBattlefieldAndReturn(player1, new Atog());
        Permanent secondAtog = harness.addToBattlefieldAndReturn(player1, new Atog());
        secondAtog.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstAtog.getId());
        assertThat(atogatog.getPowerModifier()).isZero();
        assertThat(atogatog.getToughnessModifier()).isZero();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, secondAtog.getId());
        harness.passBothPriorities();

        assertThat(atogatog.getPowerModifier()).isEqualTo(3);
        assertThat(atogatog.getToughnessModifier()).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(atogatog.getPowerModifier()).isEqualTo(4);
        assertThat(atogatog.getToughnessModifier()).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Atog"))
                .hasSize(2);
    }
}
