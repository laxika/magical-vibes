package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DrakeHatchling;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VenomousDragonfly.class, DrakeHatchling.class})
class VenomousDragonflyTest extends BaseCardTest {

    @Test
    @DisplayName("When Venomous Dragonfly becomes blocked, it destroys the blocker at end of combat")
    void becomesBlockedDestroysBlockerAtEndOfCombat() {
        Permanent dragonfly = addReadyDragonfly(player1);
        dragonfly.setAttacking(true);
        addReadyDrake(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Drake Hatchling");
    }

    @Test
    @DisplayName("When Venomous Dragonfly blocks, it destroys the attacker at end of combat")
    void blocksDestroysAttackerAtEndOfCombat() {
        Permanent drake = addReadyDrake(player1);
        drake.setAttacking(true);
        addReadyDragonfly(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Drake Hatchling");
    }

    @Test
    @DisplayName("When Venomous Dragonfly becomes blocked by multiple creatures, it destroys each blocker at end of combat")
    void becomesBlockedDestroysEachBlockerAtEndOfCombat() {
        Permanent dragonfly = addReadyDragonfly(player1);
        dragonfly.setAttacking(true);
        Permanent firstBlocker = addReadyDrake(player2);
        addReadyDrake(player2);

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
                .containsExactlyInAnyOrder("Drake Hatchling", "Drake Hatchling");
    }

    private Permanent addReadyDragonfly(Player player) {
        return addCreatureReady(player, new VenomousDragonfly());
    }

    private Permanent addReadyDrake(Player player) {
        return addCreatureReady(player, new DrakeHatchling());
    }
}
