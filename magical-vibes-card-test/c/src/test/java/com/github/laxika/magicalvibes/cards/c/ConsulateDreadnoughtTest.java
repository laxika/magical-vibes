package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConsulateDreadnought.class, AvatarOfMight.class, GrizzlyBears.class})
class ConsulateDreadnoughtTest extends BaseCardTest {

    @Test
    void isNotACreatureBeforeCrewing() {
        Permanent dreadnought = addDreadnoughtReady(player1);

        assertThat(gqs.isCreature(gd, dreadnought)).isFalse();
    }

    @Test
    void crewWithEnoughPowerAnimatesDreadnoughtAndTapsCrew() {
        Permanent dreadnought = addDreadnoughtReady(player1);
        Permanent crew = addCreatureReady(player1, new AvatarOfMight());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dreadnought.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, dreadnought)).isTrue();
        assertThat(gqs.getEffectivePower(gd, dreadnought)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, dreadnought)).isEqualTo(11);
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addDreadnoughtReady(player1);
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void crewAnimationResetsAtEndOfTurn() {
        Permanent dreadnought = addDreadnoughtReady(player1);
        addCreatureReady(player1, new AvatarOfMight());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, dreadnought)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dreadnought.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, dreadnought)).isFalse();
    }

    private Permanent addDreadnoughtReady(Player player) {
        return addCreatureReady(player, new ConsulateDreadnought());
    }

    @Test
    void crewCanCombineMultipleCreaturesToReachExactlySixPower() {
        Permanent dreadnought = addDreadnoughtReady(player1);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent third = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, dreadnought)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, dreadnought)).isTrue();
        assertThat(dreadnought.isTapped()).isFalse();
    }

    @Test
    void summoningSickCreaturesCanCrewASummoningSickVehicle() {
        Permanent dreadnought = harness.addToBattlefieldAndReturn(player1, new ConsulateDreadnought());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new AvatarOfMight());
        dreadnought.setSummoningSick(true);
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, dreadnought)).isTrue();
        assertThat(dreadnought.isSummoningSick()).isTrue();
    }

    @Test
    void tappedCreaturesCannotSupplyCrewPower() {
        addDreadnoughtReady(player1);
        Permanent crew = addCreatureReady(player1, new AvatarOfMight());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void opponentsCreaturesCannotSupplyCrewPower() {
        addDreadnoughtReady(player1);
        Permanent crew = addCreatureReady(player2, new AvatarOfMight());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(crew.isTapped()).isFalse();
    }

    @Test
    void canTapAdditionalCrewEvenAfterReachingSixPower() {
        Permanent dreadnought = addDreadnoughtReady(player1);
        Permanent first = addCreatureReady(player1, new AvatarOfMight());
        Permanent second = addCreatureReady(player1, new AvatarOfMight());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, dreadnought)).isTrue();
    }
}
