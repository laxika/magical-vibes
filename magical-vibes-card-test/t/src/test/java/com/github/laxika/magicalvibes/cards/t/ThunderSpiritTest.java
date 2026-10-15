package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.e.EmeraldDragonfly;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThunderSpirit.class, BarbaryApes.class, EmeraldDragonfly.class})
class ThunderSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a ground creature from blocking")
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent attacker = addCreatureReady(player1, new ThunderSpirit());
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("First strike destroys a flying blocker before regular damage")
    void firstStrikeDestroysBlockerBeforeRegularDamage() {
        Permanent attacker = addCreatureReady(player1, new ThunderSpirit());
        Permanent blocker = addCreatureReady(player2, new EmeraldDragonfly());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thunder Spirit");
        harness.assertInGraveyard(player2, "Emerald Dragonfly");
    }

    @Test
    @DisplayName("First strike kills an equally sized ground attacker before it deals damage")
    void firstStrikeWhileBlocking() {
        addCreatureReady(player1, new BarbaryApes());
        addCreatureReady(player2, new ThunderSpirit());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Barbary Apes");
        harness.assertOnBattlefield(player2, "Thunder Spirit");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An unblocked first striker deals damage only once")
    void unblockedFirstStrikerDealsDamageOnlyOnce() {
        addCreatureReady(player1, new ThunderSpirit());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

}
