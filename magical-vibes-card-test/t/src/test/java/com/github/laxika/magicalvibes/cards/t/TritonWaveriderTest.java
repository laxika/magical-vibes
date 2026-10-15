package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.e.EnchantedEvening;
import com.github.laxika.magicalvibes.cards.n.NyxbornSeaguard;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TritonWaverider.class, GloriousAnthem.class, NyxbornSeaguard.class, EnchantedEvening.class})
class TritonWaveriderTest extends BaseCardTest {

    @Test
    @DisplayName("An enchantment entering under your control grants flying until end of turn")
    void enchantmentTriggerGrantsFlying() {
        Permanent waverider = addCreatureReady(player1, new TritonWaverider());
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, waverider, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent waverider = addCreatureReady(player1, new TritonWaverider());
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, waverider, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, waverider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An enchantment entering under an opponent's control does not trigger it")
    void opponentEnchantmentDoesNotTrigger() {
        Permanent waverider = addCreatureReady(player1, new TritonWaverider());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GloriousAnthem(), "{1}{W}{W}");
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, waverider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Casting an enchantment does not grant flying before it enters or its trigger resolves")
    void flyingRequiresTriggerResolution() {
        Permanent waverider = addCreatureReady(player1, new TritonWaverider());
        harness.castFromHand(player1, new NyxbornSeaguard(), "{2}{U}{U}");

        assertThat(gqs.hasKeyword(gd, waverider, Keyword.FLYING)).isFalse();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, waverider, Keyword.FLYING)).isFalse();
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, waverider, Keyword.FLYING)).isTrue();
        Permanent seaguard = findPermanent(player1, "Nyxborn Seaguard");
        assertThat(gqs.hasKeyword(gd, seaguard, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An enchantment entering without being cast triggers each Waverider")
    void noncastEnchantmentTriggersEachWaverider() {
        Permanent first = addCreatureReady(player1, new TritonWaverider());
        Permanent second = addCreatureReady(player1, new TritonWaverider());

        harness.enterBattlefieldAndReturn(player1, new NyxbornSeaguard());

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A nonenchantment creature entering does not trigger constellation")
    void nonenchantmentCreatureDoesNotTrigger() {
        Permanent waverider = addCreatureReady(player1, new TritonWaverider());

        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new TritonWaverider());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, waverider, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Waverider triggers for its own entry when Enchanted Evening makes it an enchantment")
    void triggersForItsOwnEnchantmentEntry() {
        harness.addToBattlefield(player1, new EnchantedEvening());

        Permanent waverider = harness.enterBattlefieldAndReturn(player1, new TritonWaverider());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, waverider, Keyword.FLYING)).isTrue();
    }
}
