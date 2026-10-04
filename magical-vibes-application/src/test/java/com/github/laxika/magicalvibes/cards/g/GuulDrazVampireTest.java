package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuulDrazVampire.class})
class GuulDrazVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Is a 1/1 without intimidate while every opponent is above 10 life")
    void noBonusWhenOpponentsAreAboveThreshold() {
        Permanent vampire = putVampireOnBattlefield();

        assertVampire(vampire, 1, 1, false);
    }

    @Test
    @DisplayName("Gets +2/+1 and intimidate when an opponent has exactly 10 life")
    void bonusAtExactly10OpponentLife() {
        gd.playerLifeTotals.put(player2.getId(), 10);
        Permanent vampire = putVampireOnBattlefield();

        assertVampire(vampire, 3, 2, true);
    }

    @Test
    @DisplayName("Loses the bonus when the opponent rises above 10 life")
    void bonusWearsOffWhenOpponentLifeRises() {
        gd.playerLifeTotals.put(player2.getId(), 10);
        Permanent vampire = putVampireOnBattlefield();

        assertVampire(vampire, 3, 2, true);

        gd.playerLifeTotals.put(player2.getId(), 11);
        assertVampire(vampire, 1, 1, false);
    }

    @Test
    @DisplayName("Checks opponents relative to the vampire's controller")
    void thresholdIsControllerRelative() {
        gd.playerLifeTotals.put(player1.getId(), 10);
        gd.playerLifeTotals.put(player2.getId(), 20);
        Permanent vampire = harness.addToBattlefieldAndReturn(player2, new GuulDrazVampire());

        assertVampire(vampire, 3, 2, true);
    }

    @Test
    @DisplayName("Gains the bonus immediately when the opponent falls below 10 life")
    void bonusAppearsWhenOpponentLifeFalls() {
        Permanent vampire = putVampireOnBattlefield();
        assertVampire(vampire, 1, 1, false);

        harness.setLife(player2, 9);

        assertVampire(vampire, 3, 2, true);
    }

    @Test
    @DisplayName("Low controller life does not enable the bonus")
    void controllerLifeDoesNotEnableBonus() {
        harness.setLife(player1, 5);
        harness.setLife(player2, 11);
        Permanent vampire = putVampireOnBattlefield();

        assertVampire(vampire, 1, 1, false);
    }

    @Test
    @DisplayName("Each vampire gets only its own bonus, relative to its controller")
    void bonusIsSelfOnlyAndDoesNotStackAcrossVampires() {
        harness.setLife(player2, 10);
        Permanent first = putVampireOnBattlefield();
        Permanent second = putVampireOnBattlefield();
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GuulDrazVampire());

        assertVampire(first, 3, 2, true);
        assertVampire(second, 3, 2, true);
        assertVampire(opposing, 1, 1, false);
    }

    private Permanent putVampireOnBattlefield() {
        return harness.addToBattlefieldAndReturn(player1, new GuulDrazVampire());
    }

    private void assertVampire(Permanent vampire, int power, int toughness, boolean intimidate) {
        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(toughness);
        assertThat(gqs.hasKeyword(gd, vampire, Keyword.INTIMIDATE)).isEqualTo(intimidate);
    }
}
