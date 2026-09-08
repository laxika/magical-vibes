package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IrontreadCrusherTest extends BaseCardTest {

    @Test
    void isNotACreatureBeforeCrewing() {
        Permanent crusher = addIrontreadCrusherReady(player1);

        assertThat(gqs.isCreature(gd, crusher)).isFalse();
    }

    @Test
    void crewWithEnoughPowerAnimatesCrusherAndTapsCrew() {
        Permanent crusher = addIrontreadCrusherReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCrew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crusher.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, crusher)).isTrue();
        assertThat(gqs.getEffectivePower(gd, crusher)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, crusher)).isEqualTo(6);
        assertThat(crew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addIrontreadCrusherReady(player1);
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void crewAnimationResetsAtEndOfTurn() {
        Permanent crusher = addIrontreadCrusherReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, crusher)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(crusher.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, crusher)).isFalse();
    }

    private Permanent addIrontreadCrusherReady(Player player) {
        Permanent permanent = new Permanent(new IrontreadCrusher());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
