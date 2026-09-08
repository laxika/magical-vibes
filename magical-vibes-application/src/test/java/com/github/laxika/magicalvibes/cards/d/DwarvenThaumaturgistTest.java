package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CloudDjinn;
import com.github.laxika.magicalvibes.cards.w.WindingCanyons;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenThaumaturgist.class, CloudDjinn.class, WindingCanyons.class})
class DwarvenThaumaturgistTest extends BaseCardTest {

    private Permanent addThaumaturgistReady() {
        return addCreatureReady(player1, new DwarvenThaumaturgist());
    }

    @Test
    @DisplayName("Switches target creature's power and toughness")
    void switchesTargetPowerAndToughness() {
        Permanent source = addThaumaturgistReady();
        Permanent djinn = harness.addToBattlefieldAndReturn(player2, new CloudDjinn());

        harness.activateAbility(player1, 0, null, djinn.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(5);
    }

    @Test
    @DisplayName("Switch wears off at end of turn")
    void switchWearsOff() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addThaumaturgistReady();
        Permanent djinn = harness.addToBattlefieldAndReturn(player1, new CloudDjinn());

        harness.activateAbility(player1, 0, null, djinn.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addThaumaturgistReady();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new WindingCanyons());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be a creature");
    }

    @Test
    @DisplayName("Two switches cancel each other")
    void twoSwitchesCancelEachOther() {
        addThaumaturgistReady();
        addThaumaturgistReady();
        Permanent djinn = harness.addToBattlefieldAndReturn(player2, new CloudDjinn());

        harness.activateAbility(player1, 0, null, djinn.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, djinn.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(4);
    }
}
