package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SummitApes.class, Mountain.class})
class SummitApesTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have menace without a Mountain")
    void noMountainNoMenace() {
        Permanent apes = harness.addToBattlefieldAndReturn(player1, new SummitApes());

        assertThat(gqs.hasKeyword(gd, apes, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Has menace while its controller controls a Mountain")
    void hasMenaceWithMountain() {
        Permanent apes = harness.addToBattlefieldAndReturn(player1, new SummitApes());
        harness.addToBattlefield(player1, new Mountain());

        assertThat(gqs.hasKeyword(gd, apes, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's Mountain does not grant menace")
    void opponentMountainDoesNotCount() {
        Permanent apes = harness.addToBattlefieldAndReturn(player1, new SummitApes());
        harness.addToBattlefield(player2, new Mountain());

        assertThat(gqs.hasKeyword(gd, apes, Keyword.MENACE)).isFalse();
    }

    @Test
    void menaceUpdatesAsMountainsEnterAndLeave() {
        Permanent apes = harness.addToBattlefieldAndReturn(player1, new SummitApes());
        assertThat(gqs.hasKeyword(gd, apes, Keyword.MENACE)).isFalse();

        Permanent firstMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent secondMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        assertThat(gqs.hasKeyword(gd, apes, Keyword.MENACE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(firstMountain);
        assertThat(gqs.hasKeyword(gd, apes, Keyword.MENACE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(secondMountain);
        assertThat(gqs.hasKeyword(gd, apes, Keyword.MENACE)).isFalse();
    }

    @Test
    void mountainMakesOneBlockerIllegal() {
        addCreatureReady(player1, new SummitApes());
        harness.addToBattlefield(player1, new Mountain());
        addCreatureReady(player2, new SummitApes());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void mountainAllowsTwoBlockers() {
        addCreatureReady(player1, new SummitApes());
        harness.addToBattlefield(player1, new Mountain());
        Permanent first = addCreatureReady(player2, new SummitApes());
        Permanent second = addCreatureReady(player2, new SummitApes());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    void noMountainAllowsOneBlocker() {
        addCreatureReady(player1, new SummitApes());
        Permanent blocker = addCreatureReady(player2, new SummitApes());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
