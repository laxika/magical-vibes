package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MasterSplicer.class, Xenograft.class})
class MasterSplicerTest extends BaseCardTest {

    @Test
    @DisplayName("Master Splicer receives its own bonus when Xenograft makes it a Golem")
    void boostsItselfWhenItBecomesAGolem() {
        harness.addToBattlefield(player1, new MasterSplicer());
        harness.castFromHand(player1, new Xenograft(), "{4}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOLEM");

        Permanent splicer = findPermanent(player1, "Master Splicer");
        assertThat(gqs.getEffectivePower(gd, splicer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, splicer)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Master Splicers stack their bonuses on each Golem")
    void multipleSplicersStackBonuses() {
        harness.castFromHand(player1, new MasterSplicer(), "{3}{W}");
        resolveAllTriggers();
        harness.castFromHand(player1, new MasterSplicer(), "{3}{W}");
        resolveAllTriggers();

        List<Permanent> golems = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(golems).hasSize(2);
        for (Permanent golem : golems) {
            assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(5);
        }
    }

    @Test
    @DisplayName("The token trigger resolves even after Master Splicer leaves")
    void triggerResolvesWithoutSource() {
        harness.castFromHand(player1, new MasterSplicer(), "{3}{W}");
        harness.passBothPriorities();
        Permanent splicer = findPermanent(player1, "Master Splicer");
        gd.playerBattlefields.get(player1.getId()).remove(splicer);
        resolveAllTriggers();

        Permanent golem = findPermanent(player1, "Phyrexian Golem");
        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(3);
        assertThat(golem.getCard().getColor()).isNull();
        assertThat(golem.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("ETB creates a 3/3 colorless Phyrexian Golem artifact creature token")
    void etbCreatesGolemToken() {
        harness.castFromHand(player1, new MasterSplicer(), "{3}{W}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2); // Master Splicer + Golem token

        Permanent golemToken = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .findFirst()
                .orElseThrow();
        assertThat(golemToken.getCard().getSubtypes()).contains(CardSubtype.PHYREXIAN, CardSubtype.GOLEM);
        assertThat(golemToken.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(golemToken.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Golem token gets +1/+1 from Master Splicer's static ability, becoming 4/4")
    void golemTokenGetsPlusOnePlusOne() {
        harness.castFromHand(player1, new MasterSplicer(), "{3}{W}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent golemToken = findPermanent(player1, "Phyrexian Golem");

        assertThat(gqs.getEffectivePower(gd, golemToken)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, golemToken)).isEqualTo(4);
    }

    @Test
    @DisplayName("Master Splicer itself does not get +1/+1 (not a Golem)")
    void masterSplicerDoesNotGetBoost() {
        harness.addToBattlefield(player1, new MasterSplicer());

        Permanent masterSplicer = findPermanent(player1, "Master Splicer");

        assertThat(gqs.getEffectivePower(gd, masterSplicer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, masterSplicer)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's Golems do not get +1/+1")
    void opponentGolemsDoNotGetBoost() {
        harness.addToBattlefield(player1, new MasterSplicer());

        // Put a Golem on the opponent's battlefield
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new MasterSplicer(), "{3}{W}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Player 2's Golem token should be 4/4 from player 2's own Master Splicer
        // but not get a second +1/+1 from player 1's Master Splicer
        Permanent p2Golem = findPermanent(player2, "Phyrexian Golem");

        assertThat(gqs.getEffectivePower(gd, p2Golem)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, p2Golem)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost is lost when Master Splicer leaves the battlefield")
    void boostLostWhenMasterSplicerLeaves() {
        harness.castFromHand(player1, new MasterSplicer(), "{3}{W}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent golemToken = findPermanent(player1, "Phyrexian Golem");

        // Verify golem is 4/4 with Master Splicer
        assertThat(gqs.getEffectivePower(gd, golemToken)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, golemToken)).isEqualTo(4);

        // Remove Master Splicer from battlefield
        Permanent masterSplicer = findPermanent(player1, "Master Splicer");
        gd.playerBattlefields.get(player1.getId()).remove(masterSplicer);

        // Golem should be back to base 3/3
        assertThat(gqs.getEffectivePower(gd, golemToken)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, golemToken)).isEqualTo(3);
    }
}
