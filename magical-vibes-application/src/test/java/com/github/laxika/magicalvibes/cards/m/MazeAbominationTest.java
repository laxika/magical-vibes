package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
    @DisplayName("A granted-deathtouch attacker destroys its blocker with 1 damage")
    void grantedDeathtouchDestroysBlocker() {
        addCreatureReady(player1, new MazeAbomination());
        Permanent attacker = addCreatureReady(player1, new QasaliAmbusher()); // 3/4
        attacker.setAttacking(true);

        // 4/4: 3 damage is not lethal on its own, so only deathtouch can destroy it.
        Permanent blocker = addCreatureReady(player2, new AirElemental());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(gd.playerBattlefields.get(player1.getId()).indexOf(attacker));

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }
}
