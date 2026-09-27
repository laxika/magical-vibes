package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThunderSpirit.class, BarbaryApes.class})
class ThunderSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a ground creature from blocking")
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent attacker = addReadyAttacker(player1, new ThunderSpirit());
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("First strike destroys an equally sized blocker before regular damage")
    void firstStrikeDestroysBlockerBeforeRegularDamage() {
        Permanent attacker = addReadyAttacker(player1, new ThunderSpirit());
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thunder Spirit");
        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    private Permanent addReadyAttacker(Player player, Card card) {
        Permanent permanent = addCreatureReady(player, card);
        permanent.setAttacking(true);
        return permanent;
    }

}
