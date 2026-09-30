package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KondasBanner;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvacynsMemorial.class, AdelizTheCinderWind.class, GrizzlyBears.class, KondasBanner.class})
class AvacynsMemorialTest extends BaseCardTest {

    @Test
    @DisplayName("Other legendary permanents you control have indestructible")
    void grantsIndestructibleToOtherLegendaryPermanentsYouControl() {
        harness.addToBattlefield(player1, new AvacynsMemorial());
        harness.addToBattlefield(player1, new AdelizTheCinderWind());
        harness.addToBattlefield(player1, new KondasBanner());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Adeliz, the Cinder Wind"),
                Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Konda's Banner"),
                Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"),
                Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Avacyn's Memorial does not grant indestructible to an opponent's legendary permanent")
    void doesNotGrantIndestructibleToOpponentsLegendaryPermanents() {
        harness.addToBattlefield(player1, new AvacynsMemorial());
        harness.addToBattlefield(player2, new AdelizTheCinderWind());

        Permanent opponentAdeliz = findPermanent(player2, "Adeliz, the Cinder Wind");
        assertThat(gqs.hasKeyword(gd, opponentAdeliz, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The indestructible grant ends when Avacyn's Memorial leaves the battlefield")
    void grantEndsWhenSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new AvacynsMemorial());
        harness.addToBattlefield(player1, new AdelizTheCinderWind());

        Permanent adeliz = findPermanent(player1, "Adeliz, the Cinder Wind");
        assertThat(gqs.hasKeyword(gd, adeliz, Keyword.INDESTRUCTIBLE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Avacyn's Memorial"));

        assertThat(gqs.hasKeyword(gd, adeliz, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
