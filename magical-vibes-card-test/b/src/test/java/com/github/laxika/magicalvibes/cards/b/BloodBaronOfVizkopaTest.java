package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodBaronOfVizkopa.class})
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
        harness.setLife(player1, 30);
        harness.setLife(player2, 11);
        Permanent baron = putBaronOnBattlefield();

        assertBaron(baron, 4, 4, false);
    }

    @Test
    @DisplayName("No boost when only the opponent's life threshold is met")
    void noBoostWhenControllerLifeTooLow() {
        harness.setLife(player1, 29);
        harness.setLife(player2, 10);
        Permanent baron = putBaronOnBattlefield();

        assertBaron(baron, 4, 4, false);
    }

    @Test
    @DisplayName("Gets +6/+6 and flying when both thresholds are exactly met")
    void boostWhenBothThresholdsMet() {
        harness.setLife(player1, 30);
        harness.setLife(player2, 10);
        Permanent baron = putBaronOnBattlefield();

        assertBaron(baron, 10, 10, true);
    }

    @Test
    @DisplayName("Loses the boost as soon as a life total moves out of range")
    void boostWearsOffWhenLifeChanges() {
        harness.setLife(player1, 35);
        harness.setLife(player2, 5);
        Permanent baron = putBaronOnBattlefield();

        assertBaron(baron, 10, 10, true);

        harness.setLife(player2, 12);
        assertBaron(baron, 4, 4, false);
    }

    @Test
    @DisplayName("The clause reads the opponent of the baron's controller, not a fixed player")
    void thresholdsAreControllerRelative() {
        harness.setLife(player2, 30);
        harness.setLife(player1, 10);
        Permanent ownBaron = harness.addToBattlefieldAndReturn(player1, new BloodBaronOfVizkopa());
        Permanent opponentBaron = harness.addToBattlefieldAndReturn(player2, new BloodBaronOfVizkopa());

        assertBaron(ownBaron, 4, 4, false);
        assertBaron(opponentBaron, 10, 10, true);
    }

    @Test
    @DisplayName("Lifelink combat damage enables the boost only after damage is dealt")
    void combatDamageEnablesBoost() {
        harness.setLife(player1, 26);
        harness.setLife(player2, 14);
        Permanent baron = addCreatureReady(player1, new BloodBaronOfVizkopa());
        baron.setAttacking(true);

        assertBaron(baron, 4, 4, false);
        resolveCombat();

        harness.assertLife(player1, 30);
        harness.assertLife(player2, 10);
        assertBaron(baron, 10, 10, true);
    }

    @Test
    @DisplayName("Boosted combat damage gains ten life through lifelink")
    void boostedCombatDamageGainsLife() {
        harness.setLife(player1, 30);
        harness.setLife(player2, 10);
        Permanent baron = addCreatureReady(player1, new BloodBaronOfVizkopa());
        baron.setAttacking(true);

        resolveCombat();

        harness.assertLife(player1, 40);
        harness.assertLife(player2, 0);
    }

    @Test
    @DisplayName("The boost is lost and regained as the controller crosses thirty life")
    void boostTracksControllerLifeChanges() {
        harness.setLife(player1, 30);
        harness.setLife(player2, 10);
        Permanent baron = putBaronOnBattlefield();

        assertBaron(baron, 10, 10, true);
        harness.setLife(player1, 29);
        assertBaron(baron, 4, 4, false);
        harness.setLife(player1, 30);
        assertBaron(baron, 10, 10, true);
        assertThat(gqs.hasProtectionFrom(gd, baron, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, baron, CardColor.BLACK)).isTrue();
    }

    private Permanent putBaronOnBattlefield() {
        return harness.addToBattlefieldAndReturn(player1, new BloodBaronOfVizkopa());
    }

    private void assertBaron(Permanent baron, int power, int toughness, boolean flying) {
        assertThat(gqs.getEffectivePower(gd, baron)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, baron)).isEqualTo(toughness);
        assertThat(gqs.hasKeyword(gd, baron, Keyword.FLYING)).isEqualTo(flying);
    }
}
