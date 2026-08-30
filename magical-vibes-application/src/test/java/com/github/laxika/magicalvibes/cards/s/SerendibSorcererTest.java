package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerendibSorcerer.class, AirElemental.class})
class SerendibSorcererTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping it sets another target creature's base power and toughness to 0/2")
    void setsAnotherCreatureBasePowerAndToughness() {
        Permanent sorcerer = addCreatureReady(player1, new SerendibSorcerer());
        Permanent target = addCreatureReady(player2, new AirElemental());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(sorcerer.isTapped()).isTrue();
        assertThat(target.getEffectivePower()).isZero();
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The base power and toughness set wear off at cleanup")
    void wearsOffAtCleanup() {
        addCreatureReady(player1, new SerendibSorcerer());
        Permanent target = addCreatureReady(player2, new AirElemental());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target Serendib Sorcerer itself")
    void cannotTargetItself() {
        Permanent sorcerer = addCreatureReady(player1, new SerendibSorcerer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, sorcerer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature");
    }
}
