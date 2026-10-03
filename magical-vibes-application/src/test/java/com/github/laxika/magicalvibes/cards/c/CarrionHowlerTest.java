package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CarrionHowler.class})
class CarrionHowlerTest extends BaseCardTest {

    @Test
    void payingLifeBoostsCarrionHowler() {
        Permanent howler = harness.addToBattlefieldAndReturn(player1, new CarrionHowler());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gqs.getEffectivePower(gd, howler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, howler)).isEqualTo(1);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent howler = harness.addToBattlefieldAndReturn(player1, new CarrionHowler());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, howler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, howler)).isEqualTo(1);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, howler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, howler)).isEqualTo(2);
    }

    @Test
    void cannotPayActivationCostWithoutEnoughLife() {
        harness.addToBattlefield(player1, new CarrionHowler());
        harness.setLife(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    void lifeIsPaidBeforeTheBoostResolves() {
        Permanent howler = harness.addToBattlefieldAndReturn(player1, new CarrionHowler());
        harness.setLife(player1, 5);

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 4);
        assertThat(gqs.getEffectivePower(gd, howler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, howler)).isEqualTo(2);

        harness.passBothPriorities();

        harness.assertLife(player1, 4);
        assertThat(gqs.getEffectivePower(gd, howler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, howler)).isEqualTo(1);
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent howler = harness.addToBattlefieldAndReturn(player1, new CarrionHowler());
        howler.setSummoningSick(true);
        howler.setTapped(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, howler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, howler)).isEqualTo(1);
        assertThat(howler.isTapped()).isTrue();
    }

    @Test
    void secondActivationPutsHowlerInGraveyardForZeroToughness() {
        harness.addToBattlefield(player1, new CarrionHowler());
        harness.setLife(player1, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 3);
        harness.assertNotOnBattlefield(player1, "Carrion Howler");
        harness.assertInGraveyard(player1, "Carrion Howler");
    }

    @Test
    void boostAffectsOnlyTheHowlerThatActivated() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CarrionHowler());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CarrionHowler());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(1);
    }
}
