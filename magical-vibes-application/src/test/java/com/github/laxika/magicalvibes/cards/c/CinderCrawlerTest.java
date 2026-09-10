package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DazzlingBeauty;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CinderCrawler.class)
class CinderCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 when blocked")
    void pumpsWhenBlocked() {
        Permanent crawler = addCreatureReady(player1, new CinderCrawler());
        Permanent blocker = addCreatureReady(player2, new CinderCrawler());
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
        Permanent blocker = addCreatureReady(player2, new CinderCrawler());
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
    @CardUsed(DazzlingBeauty.class)
    @DisplayName("Can activate when blocked without a creature blocking it")
    void canActivateWhenBlockedWithoutBlockers() {
        Permanent crawler = addCreatureReady(player1, new CinderCrawler());
        addCreatureReady(player2, new CinderCrawler());
        crawler.setAttacking(true);
        gd.playerAutoStopSteps.put(player1.getId(), java.util.EnumSet.allOf(TurnStep.class));
        gd.playerAutoStopSteps.put(player2.getId(), java.util.EnumSet.allOf(TurnStep.class));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DazzlingBeauty()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, crawler.getId());

        assertThat(crawler.isBlockedWithoutBlockers()).isTrue();
        assertThat(crawler.isAttacking()).isTrue();

        harness.activateAbility(player1, battlefieldIndex(crawler), null, null);
        harness.passBothPriorities();

        assertThat(crawler.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent crawler = addCreatureReady(player1, new CinderCrawler());
        Permanent blocker = addCreatureReady(player2, new CinderCrawler());
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
        Permanent blocker = addCreatureReady(player2, new CinderCrawler());
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
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                battlefieldIndex(crawler))));
    }
}
