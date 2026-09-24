package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({RighteousCause.class, GlorySeeker.class})
class RighteousCauseTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when an opponent's creature attacks")
    void opponentCreatureAttacks() {
        harness.addToBattlefield(player1, new RighteousCause());
        addCreatureReady(player2, new GlorySeeker());
        harness.setLife(player1, 20);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Gains 1 life for each attacking creature")
    void triggersOncePerAttackingCreature() {
        harness.addToBattlefield(player1, new RighteousCause());
        addCreatureReady(player2, new GlorySeeker());
        addCreatureReady(player2, new GlorySeeker());
        harness.setLife(player1, 20);

        declareAttackers(player2, List.of(0, 1));
        resolveAllTriggers();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("The controller's own attacking creature also triggers it")
    void ownCreatureAttacks() {
        harness.addToBattlefield(player1, new RighteousCause());
        addCreatureReady(player1, new GlorySeeker());
        harness.setLife(player1, 20);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Gains 1 life even when the attacking creature is blocked")
    void gainsLifeWhenAttackerIsBlocked() {
        harness.addToBattlefield(player1, new RighteousCause());
        var blocker = addCreatureReady(player1, new GlorySeeker());
        var attacker = addCreatureReady(player2, new GlorySeeker());
        harness.setLife(player1, 20);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player2.getId()).indexOf(attacker))));
        resolveCombat(player2);

        harness.assertLife(player1, 21);
    }
}
