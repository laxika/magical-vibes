package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BeskirShieldmate;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FuneralLongboat.class, BeskirShieldmate.class})
class FuneralLongboatTest extends BaseCardTest {

    @Test
    void isNotACreatureBeforeCrewing() {
        Permanent longboat = addCreatureReady(player1, new FuneralLongboat());

        assertThat(gqs.isCreature(gd, longboat)).isFalse();
    }

    @Test
    void crewAnimatesLongboatAndTapsCrew() {
        Permanent longboat = addCreatureReady(player1, new FuneralLongboat());
        Permanent crew = addCreatureReady(player1, new BeskirShieldmate());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, longboat)).isTrue();
        assertThat(gqs.getEffectivePower(gd, longboat)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, longboat)).isEqualTo(3);
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addCreatureReady(player1, new FuneralLongboat());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void crewAnimationResetsAtEndOfTurn() {
        Permanent longboat = addCreatureReady(player1, new FuneralLongboat());
        addCreatureReady(player1, new BeskirShieldmate());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, longboat)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, longboat)).isFalse();
    }

    @Test
    void attackingCrewedLongboatDoesNotTapIt() {
        Permanent longboat = addCreatureReady(player1, new FuneralLongboat());
        addCreatureReady(player1, new BeskirShieldmate());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));

        assertThat(longboat.isTapped()).isFalse();
        harness.assertLife(player2, 17);
    }

    @Test
    void summoningSickCreatureCanCrewNewLongboat() {
        Permanent longboat = harness.addToBattlefieldAndReturn(player1, new FuneralLongboat());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new BeskirShieldmate());

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, longboat)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, longboat)).isTrue();
        assertThat(longboat.isTapped()).isFalse();
    }

    @Test
    void tappedCreatureCannotCrew() {
        addCreatureReady(player1, new FuneralLongboat());
        Permanent crew = addCreatureReady(player1, new BeskirShieldmate());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void opponentsCreatureCannotCrew() {
        addCreatureReady(player1, new FuneralLongboat());
        Permanent crew = addCreatureReady(player2, new BeskirShieldmate());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(crew.isTapped()).isFalse();
    }
}
