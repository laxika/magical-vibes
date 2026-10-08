package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(WallOfBlood.class)
class WallOfBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 1 life gives Wall of Blood +1/+1 until end of turn")
    void payLifeBoostsSelf() {
        Permanent wall = addCreatureReady(player1, new WallOfBlood());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability can be activated multiple times")
    void stacksBoost() {
        Permanent wall = addCreatureReady(player1, new WallOfBlood());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        Permanent wall = addCreatureReady(player1, new WallOfBlood());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(2);
    }

    @Test
    @DisplayName("Life is paid immediately but the boost waits for resolution")
    void paysLifeBeforeResolution() {
        Permanent wall = addCreatureReady(player1, new WallOfBlood());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gqs.getEffectivePower(gd, wall)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability can be activated while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfBlood());
        wall.setSummoningSick(true);
        wall.tap();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(wall.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(3);
    }

    @Test
    @DisplayName("Only the Wall whose ability was activated receives the boost")
    void boostsOnlyItsSource() {
        Permanent first = addCreatureReady(player1, new WallOfBlood());
        Permanent second = addCreatureReady(player1, new WallOfBlood());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability cannot be activated without enough life to pay")
    void cannotPayWithZeroLife() {
        Permanent wall = addCreatureReady(player1, new WallOfBlood());
        harness.setLife(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        assertThat(gd.getLife(player1.getId())).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, wall)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(2);
    }
}
