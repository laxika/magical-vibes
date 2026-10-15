package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BeetleformMage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MazeAbomination.class, QasaliAmbusher.class, GrizzlyBears.class,
        AirElemental.class, BeetleformMage.class})
class MazeAbominationTest extends BaseCardTest {

    @Test
    @DisplayName("Grants deathtouch to a multicolored creature you control, and revokes it when it leaves")
    void grantsDeathtouchToOwnMulticoloredCreature() {
        Permanent abomination = addCreatureReady(player1, new MazeAbomination());
        Permanent multicolored = addCreatureReady(player1, new QasaliAmbusher()); // {1}{G}{W}

        assertThat(gqs.hasKeyword(gd, multicolored, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(abomination);

        assertThat(gqs.hasKeyword(gd, multicolored, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Does not grant deathtouch to a monocolored creature you control")
    void doesNotGrantToMonocoloredCreature() {
        addCreatureReady(player1, new MazeAbomination());
        Permanent monocolored = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, monocolored, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Does not grant deathtouch to an opponent's multicolored creature")
    void doesNotGrantToOpponentMulticoloredCreature() {
        addCreatureReady(player1, new MazeAbomination());
        Permanent opponentMulticolored = addCreatureReady(player2, new QasaliAmbusher());

        assertThat(gqs.hasKeyword(gd, opponentMulticolored, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("A granted-deathtouch attacker destroys a blocker with nonlethal damage")
    void grantedDeathtouchDestroysBlocker() {
        addCreatureReady(player1, new MazeAbomination());
        addCreatureReady(player1, new QasaliAmbusher());

        // The 2/3 attacker cannot destroy a 4/4 blocker through ordinary lethal damage.
        Permanent blocker = addCreatureReady(player2, new AirElemental());
        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("An existing multicolored creature gains deathtouch when Maze Abomination enters")
    void grantsToCreatureAlreadyOnBattlefield() {
        Permanent creature = addCreatureReady(player1, new BeetleformMage());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();

        harness.enterBattlefieldAndReturn(player1, new MazeAbomination());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("A multicolored creature entering later immediately has deathtouch")
    void grantsToCreatureEnteringLater() {
        addCreatureReady(player1, new MazeAbomination());

        Permanent creature = harness.enterBattlefieldAndReturn(player1, new BeetleformMage());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Deathtouch remains until the last Maze Abomination leaves")
    void overlappingGrantsRemainUntilLastSourceLeaves() {
        Permanent first = addCreatureReady(player1, new MazeAbomination());
        Permanent second = addCreatureReady(player1, new MazeAbomination());
        Permanent creature = addCreatureReady(player1, new BeetleformMage());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(first);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }
}
