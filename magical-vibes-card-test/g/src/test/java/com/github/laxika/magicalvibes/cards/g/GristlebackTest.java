package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Gristleback.class)
class GristlebackTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 1 puts a +1/+1 counter on Gristleback when an opponent was dealt damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castGristleback();

        assertThat(findPermanent(player1, "Gristleback")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bloodthirst 1 does not put on a counter when no opponent was dealt damage")
    void bloodthirstDoesNotApply() {
        castGristleback();

        assertThat(findPermanent(player1, "Gristleback")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst 1 ignores damage dealt to Gristleback's controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 1);
        castGristleback();

        assertThat(findPermanent(player1, "Gristleback")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Sacrificing Gristleback gains life equal to its power")
    void sacrificeGainsLifeEqualToPower() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castGristleback();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertInGraveyard(player1, "Gristleback");
    }

    @Test
    @DisplayName("Bloodthirst adds only one counter even after multiple damage events")
    void bloodthirstCounterCountDoesNotScaleWithDamage() {
        gd.recordDamageToPlayer(player2.getId(), 5);
        gd.recordDamageToPlayer(player2.getId(), 3);
        castGristleback();

        assertThat(findPermanent(player1, "Gristleback")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bloodthirst checks damage when Gristleback enters rather than when it is cast")
    void bloodthirstChecksDamageAtEntry() {
        harness.castFromHand(player1, new Gristleback(), "{2}{G}");
        gd.recordDamageToPlayer(player2.getId(), 1);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Gristleback")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped Gristleback can be sacrificed immediately, with life gained only on resolution")
    void sacrificeIsPaidBeforeLifeGainAndNeedsNoTap() {
        castGristleback();
        findPermanent(player1, "Gristleback").tap();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Gristleback");
        harness.assertInGraveyard(player1, "Gristleback");
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    private void castGristleback() {
        harness.castFromHand(player1, new Gristleback(), "{2}{G}");
        harness.passBothPriorities();
    }
}
