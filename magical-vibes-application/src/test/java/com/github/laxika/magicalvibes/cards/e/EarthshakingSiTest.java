package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EarthshakingSi.class, GrizzlyBears.class})
class EarthshakingSiTest extends BaseCardTest {

    @Test
    void trampleDealsExcessCombatDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new EarthshakingSi());
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 3
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void trampleRequiresLethalDamageToEveryBlockerBeforeOverflow() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new EarthshakingSi());
        attacker.setAttacking(true);
        Permanent firstBlocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBlocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        firstBlocker.setBlocking(true);
        firstBlocker.addBlockingTarget(0);
        secondBlocker.setBlocking(true);
        secondBlocker.addBlockingTarget(0);

        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 2,
                secondBlocker.getId(), 1,
                player2.getId(), 2
        ))).isInstanceOf(IllegalStateException.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 2,
                secondBlocker.getId(), 2,
                player2.getId(), 1
        ));

        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void mayAssignAllCombatDamageToBlockerInsteadOfTrampling() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new EarthshakingSi());
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 5));

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    void trampleDoesNotDealExcessDamageWhenBlocking() {
        harness.setLife(player1, 20);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new EarthshakingSi());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }
}
