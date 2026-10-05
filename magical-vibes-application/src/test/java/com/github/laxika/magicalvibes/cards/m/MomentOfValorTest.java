package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MomentOfValor.class, AirElemental.class, HillGiant.class})
class MomentOfValorTest extends BaseCardTest {

    @Test
    @DisplayName("First mode untaps, boosts, and grants indestructible to a creature")
    void firstModeUntapsBoostsAndGrantsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        target.tap();

        cast(0, target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("First mode's boost and indestructible wear off at end of turn")
    void firstModeWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        cast(0, target);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Second mode destroys a creature with power 4 or greater")
    void secondModeDestroysHighPowerCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        cast(1, target);

        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Second mode rejects a creature with power less than 4")
    void secondModeRejectsLowPowerCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new MomentOfValor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("First mode protects an already untapped creature you control from destruction")
    void firstModeProtectsUntappedCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        cast(0, target);
        cast(1, target);

        harness.assertOnBattlefield(player1, "Air Elemental");
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Second mode checks current power when choosing a target")
    void secondModeDestroysCreatureWhosePowerWasIncreasedToFour() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        target.setPowerModifier(1);

        cast(1, target);

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Second mode does not destroy a creature whose power falls below four before resolution")
    void secondModeRechecksPowerOnResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new MomentOfValor()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0, 1, target.getId());

        target.setPowerModifier(-1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Moment of Valor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Second mode may destroy a creature you control")
    void secondModeDestroysCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        cast(1, target);

        harness.assertInGraveyard(player1, "Air Elemental");
    }

    private void cast(int mode, Permanent target) {
        harness.setHand(player1, List.of(new MomentOfValor()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0, mode, target.getId());
        harness.passBothPriorities();
    }
}
