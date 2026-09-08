package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BeastsOfBogardanTest extends BaseCardTest {

    @Test
    @DisplayName("Has protection from red")
    void hasProtectionFromRed() {
        Permanent beasts = putBeastsOnBattlefield();

        assertThat(gqs.hasProtectionFrom(gd, beasts, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, beasts, CardColor.WHITE)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+1 while an opponent controls a white nontoken permanent")
    void getsBonusForWhiteNontokenPermanent() {
        Permanent beasts = putBeastsOnBattlefield();
        harness.addToBattlefield(player2, new WhiteKnight());

        assertStats(beasts, 4, 4);
    }

    @Test
    @DisplayName("A white token does not grant the bonus")
    void whiteTokenDoesNotGrantBonus() {
        Permanent beasts = putBeastsOnBattlefield();
        harness.addToBattlefield(player2, createWhiteToken());

        assertStats(beasts, 3, 3);
    }

    @Test
    @DisplayName("The controller's white permanent does not grant the bonus")
    void ownWhitePermanentDoesNotGrantBonus() {
        Permanent beasts = putBeastsOnBattlefield();
        harness.addToBattlefield(player1, new WhiteKnight());

        assertStats(beasts, 3, 3);
    }

    private Permanent putBeastsOnBattlefield() {
        return harness.addToBattlefieldAndReturn(player1, new BeastsOfBogardan());
    }

    private void assertStats(Permanent beasts, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, beasts)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, beasts)).isEqualTo(toughness);
    }

    private Card createWhiteToken() {
        Card token = new Card();
        token.setName("White Token");
        token.setType(CardType.CREATURE);
        token.setColor(CardColor.WHITE);
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        return token;
    }
}
