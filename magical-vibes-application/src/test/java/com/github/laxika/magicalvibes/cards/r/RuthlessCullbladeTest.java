package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RuthlessCullbladeTest extends BaseCardTest {

    @Test
    @DisplayName("Is a 2/1 while every opponent has more than 10 life")
    void noBonusWhenOpponentsAreAboveThreshold() {
        Permanent creature = putOnBattlefield();

        assertPowerAndToughness(creature, 2, 1);
    }

    @Test
    @DisplayName("Gets +2/+1 when an opponent has exactly 10 life")
    void bonusAtExactly10OpponentLife() {
        gd.playerLifeTotals.put(player2.getId(), 10);
        Permanent creature = putOnBattlefield();

        assertPowerAndToughness(creature, 4, 2);
    }

    @Test
    @DisplayName("Loses the bonus when the opponent rises above 10 life")
    void bonusWearsOffWhenOpponentLifeRises() {
        gd.playerLifeTotals.put(player2.getId(), 10);
        Permanent creature = putOnBattlefield();

        assertPowerAndToughness(creature, 4, 2);

        gd.playerLifeTotals.put(player2.getId(), 11);
        assertPowerAndToughness(creature, 2, 1);
    }

    @Test
    @DisplayName("Checks opponents relative to the creature's controller")
    void thresholdIsControllerRelative() {
        gd.playerLifeTotals.put(player1.getId(), 10);
        gd.playerLifeTotals.put(player2.getId(), 20);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RuthlessCullblade());

        assertPowerAndToughness(creature, 4, 2);
    }

    private Permanent putOnBattlefield() {
        return harness.addToBattlefieldAndReturn(player1, new RuthlessCullblade());
    }

    private void assertPowerAndToughness(Permanent creature, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(toughness);
    }
}
