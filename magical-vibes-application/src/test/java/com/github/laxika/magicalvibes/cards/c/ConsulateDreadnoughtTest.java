package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Permanent permanent = new Permanent(new ConsulateDreadnought());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
