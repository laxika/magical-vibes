package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BruteSuit.class, GrizzlyBears.class})
class BruteSuitTest extends BaseCardTest {

    @Test
    void isNotACreatureBeforeCrewing() {
        Permanent suit = addBruteSuitReady(player1);

        assertThat(gqs.isCreature(gd, suit)).isFalse();
    }

    @Test
    void crewAnimatesBruteSuitAndTapsCrew() {
        Permanent suit = addBruteSuitReady(player1);
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
        addBruteSuitReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void crewAnimationResetsAtEndOfTurn() {
        Permanent suit = addBruteSuitReady(player1);
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, suit)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, suit)).isFalse();
    }

    private Permanent addBruteSuitReady(Player player) {
        Permanent permanent = new Permanent(new BruteSuit());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
