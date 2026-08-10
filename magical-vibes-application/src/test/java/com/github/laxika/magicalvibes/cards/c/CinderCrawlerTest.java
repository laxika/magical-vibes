package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CinderCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 when blocked")
    void pumpsWhenBlocked() {
        Permanent crawler = addCreatureReady(player1, new CinderCrawler());
        Permanent blocker = addCreatureReady(player2, new SavannahLions());
        setupBlockedCrawler(crawler, blocker);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, battlefieldIndex(crawler), null, null);
        harness.passBothPriorities();

        assertThat(crawler.getPowerModifier()).isEqualTo(1);
        assertThat(crawler.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate repeatedly while blocked")
    void canActivateRepeatedlyWhileBlocked() {
        Permanent crawler = addCreatureReady(player1, new CinderCrawler());
        Permanent blocker = addCreatureReady(player2, new SavannahLions());
        setupBlockedCrawler(crawler, blocker);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, battlefieldIndex(crawler), null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, battlefieldIndex(crawler), null, null);
        harness.passBothPriorities();

        assertThat(crawler.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate when unblocked")
    void cannotActivateWhenUnblocked() {
        Permanent crawler = addCreatureReady(player1, new CinderCrawler());
        crawler.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(crawler), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("this creature is blocked");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent crawler = addCreatureReady(player1, new CinderCrawler());
        Permanent blocker = addCreatureReady(player2, new SavannahLions());
        setupBlockedCrawler(crawler, blocker);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, battlefieldIndex(crawler), null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(crawler.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate after the declare blockers step while still blocked")
    void canActivateAfterDeclareBlockersWhileStillBlocked() {
        Permanent crawler = addCreatureReady(player1, new CinderCrawler());
        Permanent blocker = addCreatureReady(player2, new SavannahLions());
        setupBlockedCrawler(crawler, blocker);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, battlefieldIndex(crawler), null, null);
        harness.passBothPriorities();

        assertThat(crawler.getPowerModifier()).isEqualTo(1);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void setupBlockedCrawler(Permanent crawler, Permanent blocker) {
        crawler.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(battlefieldIndex(crawler));
        blocker.addBlockingTargetId(crawler.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
