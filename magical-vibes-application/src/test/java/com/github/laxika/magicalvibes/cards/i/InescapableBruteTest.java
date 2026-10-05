package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CrabappleCohort;
import com.github.laxika.magicalvibes.cards.w.WaspLancer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InescapableBrute.class, WaspLancer.class, CrabappleCohort.class})
class InescapableBruteTest extends BaseCardTest {

    @Test
    @DisplayName("At least one creature must block Inescapable Brute if able")
    void mustBeBlockedByAtLeastOne() {
        addCreatureReady(player1, new InescapableBrute());
        addCreatureReady(player2, new WaspLancer());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }

    @Test
    @DisplayName("Blocking with one creature satisfies the requirement")
    void oneBlockerSuffices() {
        addCreatureReady(player1, new InescapableBrute());
        Permanent blocker = addCreatureReady(player2, new WaspLancer());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tapped creatures are not forced to block Inescapable Brute")
    void tappedCreaturesNotForcedToBlock() {
        addCreatureReady(player1, new InescapableBrute());
        Permanent tapped = addCreatureReady(player2, new WaspLancer());
        tapped.tap();

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Inescapable Brute deals combat damage as -1/-1 counters")
    void witherDealsMinusOneMinusOneCounters() {
        Permanent blocker = addCreatureReady(player2, new CrabappleCohort());
        addCreatureReady(player1, new InescapableBrute());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("One available blocker may choose either of two Inescapable Brutes")
    void oneBlockerMayChooseBetweenTwoBrutes() {
        addCreatureReady(player1, new InescapableBrute());
        addCreatureReady(player1, new InescapableBrute());
        Permanent blocker = addCreatureReady(player2, new WaspLancer());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Additional available creatures need not block once one blocks the Brute")
    void additionalBlockerMayRemainUnused() {
        addCreatureReady(player1, new InescapableBrute());
        Permanent blocker = addCreatureReady(player2, new WaspLancer());
        Permanent unused = addCreatureReady(player2, new WaspLancer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(unused.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Wither deals ordinary life loss to a player when no blocker is available")
    void unblockedBruteDealsDamageToPlayer() {
        addCreatureReady(player1, new InescapableBrute());
        Permanent tapped = addCreatureReady(player2, new WaspLancer());
        tapped.tap();
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 17);
    }
}
