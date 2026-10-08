package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.SludgeCrawler;
import com.github.laxika.magicalvibes.cards.s.SnappingGnarlid;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KozileksSentinel.class, SludgeCrawler.class, SnappingGnarlid.class})
class KozileksSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 when you cast a colorless spell")
    void pumpsWhenColorlessSpellIsCast() {
        Permanent sentinel = addSentinel();

        harness.castFromHand(player1, new SludgeCrawler(), "{B}");
        harness.passBothPriorities();

        assertThat(sentinel.getPowerModifier()).isEqualTo(1);
        assertThat(sentinel.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Does not trigger when you cast a colored spell")
    void doesNotPumpWhenColoredSpellIsCast() {
        Permanent sentinel = addSentinel();

        harness.castFromHand(player1, new SnappingGnarlid(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(sentinel.getPowerModifier()).isZero();
        assertThat(sentinel.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent sentinel = addSentinel();

        harness.castFromHand(player1, new SludgeCrawler(), "{B}");
        harness.passBothPriorities();
        assertThat(sentinel.getPowerModifier()).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sentinel.getPowerModifier()).isZero();
        assertThat(sentinel.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Devoid spells trigger before the spell resolves")
    void devoidSpellTriggersBeforeResolving() {
        Permanent sentinel = addSentinel();

        harness.castFromHand(player1, new SludgeCrawler(), "{B}");
        assertThat(gd.stack).hasSize(2);
        assertThat(sentinel.getPowerModifier()).isZero();

        harness.passBothPriorities();

        assertThat(sentinel.getPowerModifier()).isEqualTo(1);
        assertThat(sentinel.getToughnessModifier()).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Sludge Crawler");
    }

    @Test
    @DisplayName("Each colorless spell adds another boost")
    void repeatedColorlessCastsAccumulateBoosts() {
        Permanent sentinel = addSentinel();

        harness.castFromHand(player1, new SludgeCrawler(), "{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castFromHand(player1, new SludgeCrawler(), "{B}");
        harness.passBothPriorities();

        assertThat(sentinel.getPowerModifier()).isEqualTo(2);
        assertThat(sentinel.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent's colorless spell does not trigger")
    void opponentColorlessSpellDoesNotTrigger() {
        Permanent sentinel = addSentinel();
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new SludgeCrawler(), "{B}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(sentinel.getPowerModifier()).isZero();
        assertThat(sentinel.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Casting the Sentinel does not trigger its own ability")
    void doesNotTriggerForItsOwnCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new KozileksSentinel(), "{1}{R}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent sentinel = findPermanent(player1, "Kozilek's Sentinel");
        assertThat(sentinel.getPowerModifier()).isZero();
        assertThat(sentinel.getToughnessModifier()).isZero();
    }

    private Permanent addSentinel() {
        harness.addToBattlefield(player1, new KozileksSentinel());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return findPermanent(player1, "Kozilek's Sentinel");
    }
}
