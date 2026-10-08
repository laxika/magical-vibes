package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WingSplicer.class, BladeSplicer.class, Xenograft.class})
class WingSplicerTest extends BaseCardTest {

    

    @Test
    @DisplayName("ETB creates a 3/3 colorless Phyrexian Golem artifact creature token")
    void etbCreatesGolemToken() {
        harness.castFromHand(player1, new WingSplicer(), "{3}{U}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2); // Wing Splicer + Golem token

        Permanent golemToken = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .findFirst()
                .orElseThrow();
        assertThat(golemToken.getCard().getSubtypes()).contains(CardSubtype.PHYREXIAN, CardSubtype.GOLEM);
        assertThat(golemToken.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(golemToken.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(golemToken.getEffectivePower()).isEqualTo(3);
        assertThat(golemToken.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Golem token has flying from Wing Splicer's static ability")
    void golemTokenHasFlying() {
        harness.castFromHand(player1, new WingSplicer(), "{3}{U}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent golemToken = findPermanent(player1, "Phyrexian Golem");

        assertThat(gqs.hasKeyword(gd, golemToken, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Wing Splicer itself does not have flying (not a Golem)")
    void wingSplicerDoesNotHaveFlying() {
        harness.addToBattlefield(player1, new WingSplicer());

        Permanent wingSplicer = findPermanent(player1, "Wing Splicer");

        assertThat(gqs.hasKeyword(gd, wingSplicer, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying is granted to all Golems you control, not just the token")
    void grantsFlyingToOtherGolems() {
        harness.addToBattlefield(player1, new WingSplicer());

        harness.castFromHand(player1, new WingSplicer(), "{3}{U}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> golems = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.GOLEM))
                .toList();

        assertThat(golems).isNotEmpty();
        for (Permanent golem : golems) {
            assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isTrue();
        }
    }

    @Test
    @DisplayName("Opponent's Golems do not get flying")
    void opponentGolemsDoNotGetFlying() {
        harness.addToBattlefield(player1, new WingSplicer());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new BladeSplicer(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent opponentGolem = findPermanent(player2, "Phyrexian Golem");
        assertThat(gqs.hasKeyword(gd, opponentGolem, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying is lost when Wing Splicer leaves the battlefield")
    void flyingLostWhenWingSplicerLeaves() {
        harness.castFromHand(player1, new WingSplicer(), "{3}{U}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent golemToken = findPermanent(player1, "Phyrexian Golem");

        // Verify golem has flying
        assertThat(gqs.hasKeyword(gd, golemToken, Keyword.FLYING)).isTrue();

        // Remove Wing Splicer from battlefield
        Permanent wingSplicer = findPermanent(player1, "Wing Splicer");
        gd.playerBattlefields.get(player1.getId()).remove(wingSplicer);

        // Golem should no longer have flying
        assertThat(gqs.hasKeyword(gd, golemToken, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Wing Splicer grants flying to itself when Xenograft makes it a Golem")
    void grantsFlyingToItselfWhenItBecomesAGolem() {
        Permanent splicer = harness.addToBattlefieldAndReturn(player1, new WingSplicer());
        harness.castFromHand(player1, new Xenograft(), "{4}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOLEM");

        assertThat(gqs.hasKeyword(gd, splicer, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Wing Splicer grants flying to a Golem created by another card")
    void grantsFlyingToGolemCreatedByBladeSplicer() {
        harness.castFromHand(player1, new BladeSplicer(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent golem = findPermanent(player1, "Phyrexian Golem");
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isFalse();

        harness.castFromHand(player1, new WingSplicer(), "{3}{U}");
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isTrue();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB still creates a Golem after Wing Splicer leaves the battlefield")
    void etbResolvesWithoutWingSplicer() {
        harness.castFromHand(player1, new WingSplicer(), "{3}{U}");
        harness.passBothPriorities();
        Permanent splicer = findPermanent(player1, "Wing Splicer");
        gd.playerBattlefields.get(player1.getId()).remove(splicer);
        harness.passBothPriorities();

        Permanent golem = findPermanent(player1, "Phyrexian Golem");
        assertThat(golem.getEffectivePower()).isEqualTo(3);
        assertThat(golem.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isFalse();
    }
}
