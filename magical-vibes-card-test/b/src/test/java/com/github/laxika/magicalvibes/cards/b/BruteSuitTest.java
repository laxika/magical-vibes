package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BruteSuit.class, GrizzlyBears.class})
class BruteSuitTest extends BaseCardTest {

    @Test
    void isNotACreatureBeforeCrewing() {
        Permanent suit = addCreatureReady(player1, new BruteSuit());

        assertThat(gqs.isCreature(gd, suit)).isFalse();
    }

    @Test
    void crewAnimatesBruteSuitAndTapsCrew() {
        Permanent suit = addCreatureReady(player1, new BruteSuit());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, suit)).isTrue();
        assertThat(gqs.getEffectivePower(gd, suit)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, suit)).isEqualTo(3);
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addCreatureReady(player1, new BruteSuit());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void crewAnimationResetsAtEndOfTurn() {
        Permanent suit = addCreatureReady(player1, new BruteSuit());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, suit)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, suit)).isFalse();
    }

    @Test
    void crewCostIsPaidBeforeAnimationResolves() {
        Permanent suit = addCreatureReady(player1, new BruteSuit());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, suit)).isFalse();

        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, suit)).isTrue();
    }

    @Test
    void summoningSickCreatureCanCrew() {
        Permanent suit = addCreatureReady(player1, new BruteSuit());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, suit)).isTrue();
    }

    @Test
    void cannotCrewWithTappedCreature() {
        addCreatureReady(player1, new BruteSuit());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void cannotCrewWithOpponentsCreature() {
        addCreatureReady(player1, new BruteSuit());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(opposingCreature.isTapped()).isFalse();
    }

    @Test
    void animatedSuitCannotCrewItself() {
        Permanent suit = addCreatureReady(player1, new BruteSuit());
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(suit.isTapped()).isFalse();
    }

    @Test
    void canChooseAdditionalCrewAfterMeetingRequiredPower() {
        Permanent suit = addCreatureReady(player1, new BruteSuit());
        Permanent firstCrew = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCrew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstCrew.getId());
        harness.handlePermanentChosen(player1, secondCrew.getId());
        harness.passBothPriorities();

        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, suit)).isTrue();
    }

    @Test
    void attackingCrewedSuitDoesNotTap() {
        Permanent suit = addCreatureReady(player1, new BruteSuit());
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        assertThat(suit.isTapped()).isFalse();
    }
}
