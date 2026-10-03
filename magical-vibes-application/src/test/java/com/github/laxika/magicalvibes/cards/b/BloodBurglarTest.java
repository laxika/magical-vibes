package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodBurglar.class, GreenwoodSentinel.class})
class BloodBurglarTest extends BaseCardTest {

    @Test
    @DisplayName("Has lifelink during its controller's turn only")
    void hasLifelinkDuringControllerTurnOnly() {
        Permanent burglar = addBurglarReady(player1);

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, burglar, Keyword.LIFELINK)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, burglar, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Gains life when it deals combat damage during its controller's turn")
    void gainsLifeFromCombatDamageDuringOwnTurn() {
        Permanent burglar = addBurglarReady(player1);
        burglar.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not gain life when it deals combat damage during an opponent's turn")
    void doesNotGainLifeFromCombatDamageDuringOpponentsTurn() {
        addBurglarReady(player1);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Gains life from damage to a blocker even when both creatures die")
    void gainsLifeFromDamageToBlocker() {
        Permanent burglar = addBurglarReady(player1);
        burglar.setAttacking(true);
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Blood Burglar");
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("Opponent-controlled Blood Burglar gains life on its controller's turn")
    void opponentControlledBurglarGainsLife() {
        Permanent burglar = addBurglarReady(player2);
        burglar.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat(player2);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 22);
    }

    private Permanent addBurglarReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new BloodBurglar());
        perm.setSummoningSick(false);
        return perm;
    }
}
