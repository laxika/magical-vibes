package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.ThrabenValiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeraphOfDawn.class, ThrabenValiant.class})
class SeraphOfDawnTest extends BaseCardTest {

    @Test
    void unblockedDamageGainsLifeForController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SeraphOfDawn());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void flyingRejectsGroundBlockerButAllowsFlyingBlocker() {
        Permanent seraph = addCreatureReady(player1, new SeraphOfDawn());
        Permanent groundBlocker = addCreatureReady(player2, new ThrabenValiant());
        Permanent flyingBlocker = addCreatureReady(player2, new SeraphOfDawn());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(bls.canBlockAttacker(gd, groundBlocker, seraph,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, flyingBlocker, seraph,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    void bothControllersGainLifeWhenSeraphsDealDamageToEachOther() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SeraphOfDawn());
        addCreatureReady(player2, new SeraphOfDawn());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 22);
        harness.assertOnBattlefield(player1, "Seraph of Dawn");
        harness.assertOnBattlefield(player2, "Seraph of Dawn");
    }

    @Test
    void blockingGroundAttackerGainsLifeForDefenderIncludingExcessDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ThrabenValiant());
        addCreatureReady(player2, new SeraphOfDawn());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 22);
        harness.assertInGraveyard(player1, "Thraben Valiant");
        harness.assertOnBattlefield(player2, "Seraph of Dawn");
    }
}
