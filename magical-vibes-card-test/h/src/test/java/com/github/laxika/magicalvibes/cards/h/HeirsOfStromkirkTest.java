package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FarbogExplorer;
import com.github.laxika.magicalvibes.cards.n.NarstadScrapper;
import com.github.laxika.magicalvibes.cards.r.RiotRingleader;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeirsOfStromkirk.class, RiotRingleader.class, FarbogExplorer.class, NarstadScrapper.class})
class HeirsOfStromkirkTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when dealing combat damage to a player")
    void getsCounterOnCombatDamage() {
        Permanent heirs = addCreatureReady(player1, new HeirsOfStromkirk());
        heirs.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        harness.passBothPriorities(); // resolve trigger
        assertThat(heirs.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No counter when blocked and no damage reaches the player")
    void noCounterWhenBlocked() {
        Permanent heirs = addCreatureReady(player1, new HeirsOfStromkirk());
        heirs.setAttacking(true);
        harness.setLife(player2, 20);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RiotRingleader());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(heirs.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Intimidate rejects a nonartifact blocker that shares no color")
    void rejectsWhiteBlocker() {
        addCreatureReady(player1, new HeirsOfStromkirk());
        harness.addToBattlefield(player2, new FarbogExplorer());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("intimidate");
    }

    @Test
    @DisplayName("Intimidate allows a red blocker")
    void allowsRedBlocker() {
        Permanent heirs = addCreatureReady(player1, new HeirsOfStromkirk());
        harness.addToBattlefield(player2, new RiotRingleader());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(heirs.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Intimidate allows a colorless artifact creature to block")
    void allowsArtifactBlocker() {
        Permanent heirs = addCreatureReady(player1, new HeirsOfStromkirk());
        harness.addToBattlefield(player2, new NarstadScrapper());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(heirs.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Combat damage adds exactly one counter to its source after the trigger resolves")
    void addsOneCounterRegardlessOfDamageAmount() {
        Permanent heirs = addCreatureReady(player1, new HeirsOfStromkirk());
        Permanent otherHeirs = addCreatureReady(player1, new HeirsOfStromkirk());
        heirs.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        heirs.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 16);
        assertThat(heirs.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(heirs.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(otherHeirs.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

}
