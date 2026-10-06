package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlightKeeper;
import com.github.laxika.magicalvibes.cards.q.QueensBaySoldier;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SleekSchooner.class, QueensBaySoldier.class, BlightKeeper.class})
class SleekSchoonerTest extends BaseCardTest {

    @Test
    @DisplayName("Schooner is not a creature before crewing")
    void notACreatureBeforeCrew() {
        Permanent schooner = addCreatureReady(player1, new SleekSchooner());

        assertThat(gqs.isCreature(gd, schooner)).isFalse();
    }

    @Test
    @DisplayName("Crewing with a creature of power >= 1 animates Schooner")
    void crewWithSufficientPower() {
        Permanent schooner = addCreatureReady(player1, new SleekSchooner());
        Permanent crew = addCreatureReady(player1, new QueensBaySoldier());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(schooner.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, schooner)).isTrue();
        assertThat(gqs.getEffectivePower(gd, schooner)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, schooner)).isEqualTo(3);
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature with exactly one power is sufficient to crew")
    void crewWithExactlyOnePower() {
        Permanent schooner = addCreatureReady(player1, new SleekSchooner());
        Permanent crew = addCreatureReady(player1, new BlightKeeper());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, schooner)).isTrue();
    }

    @Test
    @DisplayName("Cannot crew without any creatures")
    void cannotCrewWithoutCreatures() {
        addCreatureReady(player1, new SleekSchooner());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    @DisplayName("Crew animation resets at end of turn")
    void crewResetsAtEndOfTurn() {
        Permanent schooner = addCreatureReady(player1, new SleekSchooner());
        addCreatureReady(player1, new QueensBaySoldier());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, schooner)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(schooner.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, schooner)).isFalse();
    }

    @Test
    @DisplayName("Crew does not require the vehicle to tap")
    void crewDoesNotTapVehicle() {
        Permanent schooner = addCreatureReady(player1, new SleekSchooner());
        addCreatureReady(player1, new QueensBaySoldier());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(schooner.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Crew taps its creature as a cost before the Vehicle becomes a creature")
    void crewCostIsPaidBeforeResolution() {
        Permanent schooner = addCreatureReady(player1, new SleekSchooner());
        Permanent crew = addCreatureReady(player1, new QueensBaySoldier());

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, schooner)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, schooner)).isTrue();
    }

    @Test
    @DisplayName("A summoning-sick creature can crew a summoning-sick Vehicle")
    void summoningSicknessDoesNotPreventCrew() {
        Permanent schooner = harness.addToBattlefieldAndReturn(player1, new SleekSchooner());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new QueensBaySoldier());
        schooner.setSummoningSick(true);
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, schooner)).isTrue();
        assertThat(schooner.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("A tapped Vehicle can still be crewed")
    void tappedVehicleCanBeCrewed() {
        Permanent schooner = addCreatureReady(player1, new SleekSchooner());
        schooner.tap();
        addCreatureReady(player1, new QueensBaySoldier());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, schooner)).isTrue();
        assertThat(schooner.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped creatures and opposing creatures cannot pay the crew cost")
    void cannotCrewWithTappedOrOpposingCreatures() {
        addCreatureReady(player1, new SleekSchooner());
        addCreatureReady(player1, new QueensBaySoldier()).tap();
        Permanent opponentCreature = addCreatureReady(player2, new QueensBaySoldier());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");

        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An animated Vehicle cannot tap itself to pay its own crew cost")
    void animatedVehicleCannotCrewItself() {
        Permanent schooner = addCreatureReady(player1, new SleekSchooner());
        addCreatureReady(player1, new QueensBaySoldier());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");

        assertThat(gqs.isCreature(gd, schooner)).isTrue();
        assertThat(schooner.isTapped()).isFalse();
    }

}
