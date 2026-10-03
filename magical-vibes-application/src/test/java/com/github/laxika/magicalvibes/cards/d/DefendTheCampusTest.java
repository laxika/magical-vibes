package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DefendTheCampus.class, AirElemental.class, GrizzlyBears.class, HillGiant.class})
class DefendTheCampusTest extends BaseCardTest {

    @Test
    void boostsOnlyCreaturesYouControl() {
        Permanent mine = addCreatureReady(player1, new GrizzlyBears());
        Permanent theirs = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DefendTheCampus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(mine.getPowerModifier()).isEqualTo(1);
        assertThat(mine.getToughnessModifier()).isEqualTo(1);
        assertThat(theirs.getPowerModifier()).isZero();
        assertThat(theirs.getToughnessModifier()).isZero();
    }

    @Test
    void destroysCreatureWithPowerAtLeastFour() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new DefendTheCampus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, 1, List.of(elemental.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    void cannotTargetCreatureWithPowerLessThanFour() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DefendTheCampus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(giant.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boostExpiresAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DefendTheCampus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    void boostsCreaturesPresentAtResolutionButNotThoseEnteringLater() {
        harness.setHand(player1, List.of(new DefendTheCampus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, 0, List.of());
        Permanent presentAtResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        Permanent laterArrival = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, presentAtResolution)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, presentAtResolution)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, laterArrival)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterArrival)).isEqualTo(2);
    }

    @Test
    void boostModeResolvesWithoutAnyCreatures() {
        harness.setHand(player1, List.of(new DefendTheCampus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Defend the Campus");
    }

    @Test
    void destroyModeCanTargetYourOwnCreatureWhosePowerWasBoostedToFour() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new DefendTheCampus(), new DefendTheCampus()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.castModalInstant(player1, 0, 1, List.of(giant.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    void destroyModeDoesNotDestroyTargetWhosePowerFallsBelowFourBeforeResolution() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new DefendTheCampus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, 1, List.of(elemental.getId()));
        elemental.setPowerModifier(-1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertNotInGraveyard(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Defend the Campus");
        assertThat(gd.stack).isEmpty();
    }
}
