package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HumblerOfMortals.class, GloriousAnthem.class, GrizzlyBears.class})
class HumblerOfMortalsTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry gives your creatures trample until end of turn")
    void ownEntryGrantsTrampleToYourCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castHumblerOfMortals();
        harness.passBothPriorities();
        Permanent humbler = findHumbler();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, humbler, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Another enchantment entering under your control grants your creatures trample")
    void anotherEnchantmentEntryGrantsTrample() {
        Permanent humbler = harness.addToBattlefieldAndReturn(player1, new HumblerOfMortals());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, humbler, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's enchantment entering does not trigger it")
    void opponentEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new HumblerOfMortals());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Trample wears off at the end of the turn")
    void trampleWearsOffAtEndOfTurn() {
        Permanent humbler = harness.addToBattlefieldAndReturn(player1, new HumblerOfMortals());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, humbler, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, humbler, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Another enchantment creates one constellation ability that grants trample to all your creatures")
    void anotherEnchantmentCreatesOneTrigger() {
        Permanent humbler = harness.addToBattlefieldAndReturn(player1, new HumblerOfMortals());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, humbler, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingBears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A nonenchantment creature entering does not trigger constellation")
    void nonenchantmentCreatureDoesNotTrigger() {
        Permanent humbler = harness.addToBattlefieldAndReturn(player1, new HumblerOfMortals());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, humbler, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after constellation resolves do not gain trample")
    void laterCreaturesDoNotGainTrample() {
        castHumblerOfMortals();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, findHumbler(), Keyword.TRAMPLE)).isTrue();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        Permanent bears = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Humbler controlled by an opponent when its trigger resolves does not gain trample")
    void sourceChangingControllerBeforeResolutionDoesNotGainTrample() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castHumblerOfMortals();
        harness.passBothPriorities();
        Permanent humbler = findHumbler();

        gd.playerBattlefields.get(player1.getId()).remove(humbler);
        gd.playerBattlefields.get(player2.getId()).add(humbler);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, humbler, Keyword.TRAMPLE)).isFalse();
    }

    private void castHumblerOfMortals() {
        harness.castFromHand(player1, new HumblerOfMortals(), "{4}{G}{G}");
    }

    private Permanent findHumbler() {
        return gqs.findPermanentById(gd, harness.getPermanentId(player1, "Humbler of Mortals"));
    }
}
