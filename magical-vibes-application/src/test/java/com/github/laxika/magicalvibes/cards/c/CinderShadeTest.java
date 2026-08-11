package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CinderShadeTest extends BaseCardTest {

    @Test
    @DisplayName("Black ability gives Cinder Shade +1/+1 until end of turn")
    void blackAbilityBoostsSelfUntilEndOfTurn() {
        Permanent shade = addCreatureReady(player1, new CinderShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shade.getPowerModifier()).isEqualTo(1);
        assertThat(shade.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shade.getPowerModifier()).isZero();
        assertThat(shade.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Red ability sacrifices Cinder Shade and deals damage equal to its power")
    void redAbilityDealsPowerDamageAndSacrificesSelf() {
        addCreatureReady(player1, new CinderShade());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Cinder Shade");
        harness.assertInGraveyard(player1, "Cinder Shade");

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Red ability uses Cinder Shade's pumped power after it is sacrificed")
    void redAbilityUsesLastKnownPumpedPower() {
        addCreatureReady(player1, new CinderShade());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Red ability cannot target a noncreature permanent")
    void redAbilityCannotTargetLand() {
        addCreatureReady(player1, new CinderShade());
        harness.addToBattlefield(player2, new Forest());
        Permanent target = findPermanent(player2, "Forest");
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
