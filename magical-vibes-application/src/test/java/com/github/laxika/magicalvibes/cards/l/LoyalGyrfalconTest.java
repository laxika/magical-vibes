package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.Endure;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoyalGyrfalcon.class, SuntailHawk.class, GrizzlyBears.class, Endure.class})
class LoyalGyrfalconTest extends BaseCardTest {

    @BeforeEach
    void setUpTest() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Casting a white spell makes the Gyrfalcon lose defender")
    void whiteSpellRemovesDefender() {
        Permanent falcon = addCreatureReady(player1, new LoyalGyrfalcon());
        assertThat(gqs.hasKeyword(gd, falcon, Keyword.DEFENDER)).isTrue();

        harness.castFromHand(player1, new SuntailHawk(), "{W}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, falcon, Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("Casting a non-white spell leaves defender in place")
    void nonWhiteSpellKeepsDefender() {
        Permanent falcon = addCreatureReady(player1, new LoyalGyrfalcon());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, falcon, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Casting a white noncreature spell makes the Gyrfalcon lose defender")
    void whiteNoncreatureSpellRemovesDefender() {
        Permanent falcon = addCreatureReady(player1, new LoyalGyrfalcon());

        harness.castFromHand(player1, new Endure(), "{3}{W}{W}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, falcon, Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("A white spell cast by an opponent does not remove defender")
    void opponentWhiteSpellKeepsDefender() {
        Permanent falcon = addCreatureReady(player1, new LoyalGyrfalcon());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new Endure(), "{3}{W}{W}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, falcon, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Defender returns at end of turn")
    void defenderReturnsAtEndOfTurn() {
        Permanent falcon = addCreatureReady(player1, new LoyalGyrfalcon());

        harness.castFromHand(player1, new SuntailHawk(), "{W}");
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, falcon, Keyword.DEFENDER)).isFalse();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, falcon, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Defender is removed only when the cast trigger resolves, before the spell")
    void defenderRemovalWaitsForTriggerResolution() {
        Permanent falcon = addCreatureReady(player1, new LoyalGyrfalcon());

        harness.castFromHand(player1, new SuntailHawk(), "{W}");

        assertThat(gqs.hasKeyword(gd, falcon, Keyword.DEFENDER)).isTrue();
        harness.assertNotOnBattlefield(player1, "Suntail Hawk");

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, falcon, Keyword.DEFENDER)).isFalse();
        harness.assertNotOnBattlefield(player1, "Suntail Hawk");
    }

    @Test
    @DisplayName("Casting another Gyrfalcon affects the existing one but not the arriving one")
    void newlyCastGyrfalconDoesNotTriggerForItself() {
        Permanent existing = addCreatureReady(player1, new LoyalGyrfalcon());
        LoyalGyrfalcon arrivingCard = new LoyalGyrfalcon();

        harness.castFromHand(player1, arrivingCard, "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent arriving = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(arrivingCard.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, existing, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, arriving, Keyword.DEFENDER)).isTrue();
    }
}
