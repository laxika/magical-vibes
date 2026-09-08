package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EndlessCockroaches;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreadCharge.class, EndlessCockroaches.class, GrizzlyBears.class})
class DreadChargeTest extends BaseCardTest {

    @Test
    @DisplayName("A black creature you control can't be blocked by a non-black creature")
    void blackCreatureCannotBeBlockedByNonBlack() {
        Permanent attacker = addCreatureReady(player1, new EndlessCockroaches());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        resolveDreadCharge();

        prepareDeclareBlockers(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by black creatures");
    }

    @Test
    @DisplayName("A black creature you control can still be blocked by a black creature")
    void blackCreatureCanBeBlockedByBlack() {
        Permanent attacker = addCreatureReady(player1, new EndlessCockroaches());
        addCreatureReady(player2, new EndlessCockroaches());
        resolveDreadCharge();

        prepareDeclareBlockers(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("A non-black creature you control is unaffected and can be blocked normally")
    void nonBlackCreatureIsUnaffected() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        resolveDreadCharge();

        prepareDeclareBlockers(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("A black creature controlled by an opponent is unaffected")
    void opponentsBlackCreatureIsUnaffected() {
        Permanent attacker = addCreatureReady(player2, new EndlessCockroaches());
        addCreatureReady(player1, new GrizzlyBears());
        resolveDreadCharge();

        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("A black creature that enters after Dread Charge resolves is still restricted")
    void blackCreatureEnteringAfterResolutionIsRestricted() {
        resolveDreadCharge();

        Permanent attacker = addCreatureReady(player1, new EndlessCockroaches());
        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by black creatures");
    }

    private void resolveDreadCharge() {
        harness.castFromHand(player1, new DreadCharge(), "{3}{B}");
        harness.passBothPriorities();
    }

    private void prepareDeclareBlockers(Permanent attacker) {
        attacker.setAttacking(true);
        prepareDeclareBlockers();
    }
}
