package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BloodBaronOfVizkopaTest extends BaseCardTest {

    @Test
    @DisplayName("Has protection from white and from black, but not from other colors")
    void hasProtectionFromWhiteAndBlack() {
        Permanent baron = putBaronOnBattlefield();

        assertThat(gqs.hasProtectionFrom(gd, baron, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, baron, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, baron, CardColor.RED)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, baron, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, baron, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Is a 4/4 without flying at default life totals")
    void noBoostAtDefaultLifeTotals() {
        Permanent baron = putBaronOnBattlefield();

        assertBaron(baron, 4, 4, false);
    }

    @Test
    @DisplayName("No boost when only the controller's life threshold is met")
    void noBoostWhenOpponentLifeTooHigh() {
        gd.playerLifeTotals.put(player1.getId(), 30);
        gd.playerLifeTotals.put(player2.getId(), 11);
        Permanent baron = putBaronOnBattlefield();

        assertBaron(baron, 4, 4, false);
    }

    @Test
    @DisplayName("No boost when only the opponent's life threshold is met")
    void noBoostWhenControllerLifeTooLow() {
        gd.playerLifeTotals.put(player1.getId(), 29);
        gd.playerLifeTotals.put(player2.getId(), 10);
        Permanent baron = putBaronOnBattlefield();

        assertBaron(baron, 4, 4, false);
    }

    @Test
    @DisplayName("Gets +6/+6 and flying when both thresholds are exactly met")
    void boostWhenBothThresholdsMet() {
        gd.playerLifeTotals.put(player1.getId(), 30);
        gd.playerLifeTotals.put(player2.getId(), 10);
        Permanent baron = putBaronOnBattlefield();

        assertBaron(baron, 10, 10, true);
    }

    @Test
    @DisplayName("Loses the boost as soon as a life total moves out of range")
    void boostWearsOffWhenLifeChanges() {
        gd.playerLifeTotals.put(player1.getId(), 35);
        gd.playerLifeTotals.put(player2.getId(), 5);
        Permanent baron = putBaronOnBattlefield();

        assertBaron(baron, 10, 10, true);

        gd.playerLifeTotals.put(player2.getId(), 12);
        assertBaron(baron, 4, 4, false);
    }

    @Test
    @DisplayName("The clause reads the opponent of the baron's controller, not a fixed player")
    void thresholdsAreControllerRelative() {
        gd.playerLifeTotals.put(player2.getId(), 30);
        gd.playerLifeTotals.put(player1.getId(), 10);
        Permanent ownBaron = harness.addToBattlefieldAndReturn(player1, new BloodBaronOfVizkopa());
        Permanent opponentBaron = harness.addToBattlefieldAndReturn(player2, new BloodBaronOfVizkopa());

        assertBaron(ownBaron, 4, 4, false);
        assertBaron(opponentBaron, 10, 10, true);
    }

    private Permanent putBaronOnBattlefield() {
        return harness.addToBattlefieldAndReturn(player1, new BloodBaronOfVizkopa());
    }

    private void assertBaron(Permanent baron, int power, int toughness, boolean flying) {
        assertThat(gqs.getEffectivePower(gd, baron)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, baron)).isEqualTo(toughness);
        assertThat(gqs.hasKeyword(gd, baron, Keyword.FLYING)).isEqualTo(flying);
        assertThat(gqs.hasKeyword(gd, baron, Keyword.LIFELINK)).isTrue();
    }
}
