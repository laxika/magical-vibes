package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.Roterothopter;
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

@CardUsed({AysenHighway.class, AysenCrusader.class, AnHavvaTownship.class, Roterothopter.class})
class AysenHighwayTest extends BaseCardTest {

    @Test
    @DisplayName("White creatures gain plainswalk, including the opponent's")
    void grantsPlainswalkToWhiteCreatures() {
        harness.addToBattlefield(player1, new AysenHighway());
        harness.addToBattlefield(player1, new AysenCrusader());
        harness.addToBattlefield(player2, new AysenCrusader());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Aysen Crusader"), Keyword.PLAINSWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Aysen Crusader"), Keyword.PLAINSWALK)).isTrue();
    }

    @Test
    @DisplayName("Non-white creatures do not gain plainswalk")
    void doesNotGrantToNonWhiteCreatures() {
        harness.addToBattlefield(player1, new AysenHighway());
        harness.addToBattlefield(player1, new Roterothopter());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Roterothopter"), Keyword.PLAINSWALK)).isFalse();
    }

    @Test
    @DisplayName("Grant is removed when Aysen Highway leaves the battlefield")
    void grantRemovedWhenSourceLeaves() {
        Permanent highway = harness.addToBattlefieldAndReturn(player1, new AysenHighway());
        harness.addToBattlefield(player1, new AysenCrusader());

        Permanent crusader = findPermanent(player1, "Aysen Crusader");
        assertThat(gqs.hasKeyword(gd, crusader, Keyword.PLAINSWALK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(highway);

        assertThat(gqs.hasKeyword(gd, crusader, Keyword.PLAINSWALK)).isFalse();
    }

    @Test
    @CardUsed(Plains.class)
    @DisplayName("White creature can't be blocked when defender controls a Plains")
    void plainswalkPreventsBlockingWithPlains() {
        harness.addToBattlefield(player1, new AysenHighway());
        harness.addToBattlefield(player2, new Plains());

        Permanent blocker = declareCombat();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(blockAssignment(blocker))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("White creature can be blocked when defender controls no Plains, even with another land")
    void plainswalkAllowsBlockingWithoutPlains() {
        harness.addToBattlefield(player1, new AysenHighway());
        harness.addToBattlefield(player2, new AnHavvaTownship());

        Permanent blocker = declareCombat();

        gs.declareBlockers(gd, player2, List.of(blockAssignment(blocker)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent declareCombat() {
        Permanent attacker = addCreatureReady(player1, new AysenCrusader());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new Roterothopter());
        prepareDeclareBlockers();

        return blocker;
    }

    private BlockerAssignment blockAssignment(Permanent blocker) {
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Aysen Crusader"));
        return new BlockerAssignment(blockerIdx, attackerIdx);
    }
}
