package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DomriRade;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChargingWarBoar.class, DomriRade.class})
class ChargingWarBoarTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 and trample while its controller controls Domri")
    void getsBonusWithDomri() {
        Permanent boar = harness.addToBattlefieldAndReturn(player1, new ChargingWarBoar());
        harness.addToBattlefield(player1, new DomriRade());

        assertThat(gqs.getEffectivePower(gd, boar)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, boar)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, boar, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not get the bonus without Domri")
    void noBonusWithoutDomri() {
        Permanent boar = harness.addToBattlefieldAndReturn(player1, new ChargingWarBoar());

        assertThat(gqs.getEffectivePower(gd, boar)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, boar)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, boar, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Does not count an opponent's Domri")
    void opponentDomriDoesNotCount() {
        Permanent boar = harness.addToBattlefieldAndReturn(player1, new ChargingWarBoar());
        harness.addToBattlefield(player2, new DomriRade());

        assertThat(gqs.getEffectivePower(gd, boar)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, boar)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, boar, Keyword.TRAMPLE)).isFalse();
    }
}
