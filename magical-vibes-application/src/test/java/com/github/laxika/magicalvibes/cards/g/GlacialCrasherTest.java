package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlacialCrasher.class, Mountain.class, Island.class, RuneclawBear.class})
class GlacialCrasherTest extends BaseCardTest {

    @Test
    @DisplayName("Glacial Crasher can attack when there is a Mountain on the battlefield")
    void canAttackWhenMountainIsOnBattlefield() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Mountain());
        addCreatureReady(player1, new GlacialCrasher());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Glacial Crasher cannot attack when there is no Mountain on the battlefield")
    void cannotAttackWithoutMountain() {
        addCreatureReady(player1, new GlacialCrasher());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canAttackWithOwnTappedMountain() {
        addCreatureReady(player1, new GlacialCrasher());
        harness.addToBattlefieldAndReturn(player1, new Mountain()).setTapped(true);

        declareAttackers(player1, List.of(0));

        harness.assertLife(player2, 15);
    }

    @Test
    void mountainInHandOrGraveyardDoesNotPermitAttacking() {
        addCreatureReady(player1, new GlacialCrasher());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setGraveyard(player2, List.of(new Mountain()));

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBlockWithoutMountain() {
        addCreatureReady(player1, new RuneclawBear());
        addCreatureReady(player2, new GlacialCrasher());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertOnBattlefield(player2, "Glacial Crasher");
    }

    @Test
    void trampleDealsExcessDamageToDefendingPlayer() {
        addCreatureReady(player1, new GlacialCrasher());
        Permanent blocker = addCreatureReady(player2, new RuneclawBear());
        harness.addToBattlefield(player2, new Mountain());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 3));

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Runeclaw Bear");
        harness.assertOnBattlefield(player1, "Glacial Crasher");
    }
}
