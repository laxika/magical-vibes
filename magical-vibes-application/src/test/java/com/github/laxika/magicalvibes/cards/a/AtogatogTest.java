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
        harness.addToBattlefield(player1, new Atogatog());
        harness.addToBattlefield(player1, new Atog());

        Permanent atogatog = findPermanent(player1, "Atogatog");
        Permanent atog = findPermanent(player1, "Atog");

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
        harness.addToBattlefield(player1, new Atogatog());
        Permanent atog = addCreatureReady(player1, new Atog());
        atog.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        Permanent atogatog = findPermanent(player1, "Atogatog");

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
        harness.addToBattlefield(player1, new Atogatog());
        Permanent atog = addCreatureReady(player1, new Atog());
        Permanent atogatog = findPermanent(player1, "Atogatog");

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
}
