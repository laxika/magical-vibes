package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CheeringCrowd;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WraithViciousVigilante.class, CheeringCrowd.class})
class WraithViciousVigilanteTest extends BaseCardTest {

    @Test
    @DisplayName("Wraith can't be blocked")
    void cannotBeBlocked() {
        Permanent wraith = addCreatureReady(player1, new WraithViciousVigilante());
        wraith.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CheeringCrowd());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Wraith deals combat damage in both combat damage steps")
    void doubleStrikeDealsDamageTwice() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new WraithViciousVigilante());

        declareAttackers(List.of(0));

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Wraith can block and deals damage in both steps while blocking")
    void canBlockAndTradeWithTwoToughnessAttacker() {
        addCreatureReady(player1, new CheeringCrowd());
        addCreatureReady(player2, new WraithViciousVigilante());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Cheering Crowd");
        harness.assertInGraveyard(player2, "Wraith, Vicious Vigilante");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Wraith's evasion does not prevent blocking another attacker")
    void otherAttackersCanStillBeBlocked() {
        addCreatureReady(player1, new WraithViciousVigilante());
        addCreatureReady(player1, new CheeringCrowd());
        addCreatureReady(player2, new CheeringCrowd());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Wraith, Vicious Vigilante");
        harness.assertInGraveyard(player1, "Cheering Crowd");
        harness.assertInGraveyard(player2, "Cheering Crowd");
        harness.assertLife(player2, 18);
    }

}
