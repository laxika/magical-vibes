package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BearerOfMemory;
import com.github.laxika.magicalvibes.cards.c.CoilingStalker;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FuturistSentinel.class, CoilingStalker.class, BearerOfMemory.class})
class FuturistSentinelTest extends BaseCardTest {

    @Test
    void isNotACreatureBeforeCrewing() {
        Permanent sentinel = addCreatureReady(player1, new FuturistSentinel());

        assertThat(gqs.isCreature(gd, sentinel)).isFalse();
    }

    @Test
    void crewAnimatesSentinelAndTapsCrew() {
        Permanent sentinel = addCreatureReady(player1, new FuturistSentinel());
        Permanent firstCrew = addCreatureReady(player1, new CoilingStalker());
        Permanent secondCrew = addCreatureReady(player1, new CoilingStalker());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, sentinel)).isTrue();
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(6);
        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addCreatureReady(player1, new FuturistSentinel());
        addCreatureReady(player1, new CoilingStalker());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void crewAnimationResetsAtEndOfTurn() {
        Permanent sentinel = addCreatureReady(player1, new FuturistSentinel());
        addCreatureReady(player1, new CoilingStalker());
        addCreatureReady(player1, new CoilingStalker());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, sentinel)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, sentinel)).isFalse();
    }

    @Test
    void summoningSickCreatureCanPayCrewAndAnimationWaitsForResolution() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new FuturistSentinel());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new BearerOfMemory());

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(sentinel.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, sentinel)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, sentinel)).isTrue();
        assertThat(sentinel.isSummoningSick()).isTrue();
    }

    @Test
    void tappedCreaturesAndOpponentsCreaturesCannotPayCrew() {
        addCreatureReady(player1, new FuturistSentinel());
        Permanent tappedCrew = addCreatureReady(player1, new BearerOfMemory());
        tappedCrew.tap();
        Permanent opposingCrew = addCreatureReady(player2, new BearerOfMemory());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");

        assertThat(opposingCrew.isTapped()).isFalse();
    }

    @Test
    void mayTapAdditionalCrewAfterMeetingRequiredPower() {
        Permanent sentinel = addCreatureReady(player1, new FuturistSentinel());
        Permanent firstCrew = addCreatureReady(player1, new BearerOfMemory());
        Permanent secondCrew = addCreatureReady(player1, new BearerOfMemory());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstCrew.getId());
        harness.handlePermanentChosen(player1, secondCrew.getId());
        harness.passBothPriorities();

        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, sentinel)).isTrue();
    }

    @Test
    void animatedSentinelCannotCrewItself() {
        Permanent sentinel = addCreatureReady(player1, new FuturistSentinel());
        addCreatureReady(player1, new BearerOfMemory());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, sentinel)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }
}
