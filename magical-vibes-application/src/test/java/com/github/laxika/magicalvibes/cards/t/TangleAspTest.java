package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TangleAsp.class, Arachnoid.class})
class TangleAspTest extends BaseCardTest {

    @Test
    @DisplayName("When Tangle Asp becomes blocked, it destroys the blocker at end of combat")
    void becomesBlockedDestroysBlockerAtEndOfCombat() {
        Permanent asp = addCreatureReady(player1, new TangleAsp());
        asp.setAttacking(true);
        addCreatureReady(player2, new Arachnoid());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Arachnoid");
    }

    @Test
    @DisplayName("When Tangle Asp blocks, it destroys the attacker at end of combat")
    void blocksDestroysAttackerAtEndOfCombat() {
        Permanent arachnoid = addCreatureReady(player1, new Arachnoid());
        arachnoid.setAttacking(true);
        addCreatureReady(player2, new TangleAsp());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Arachnoid");
    }

    @Test
    @DisplayName("When Tangle Asp becomes blocked by multiple creatures, it destroys each blocker at end of combat")
    void becomesBlockedDestroysEachBlockerAtEndOfCombat() {
        Permanent asp = addCreatureReady(player1, new TangleAsp());
        asp.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new Arachnoid());
        addCreatureReady(player2, new Arachnoid());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);

        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(firstBlocker.getId(), 1));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Arachnoid", "Arachnoid");
    }
}
