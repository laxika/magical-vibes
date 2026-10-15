package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeepSeaSerpent.class, Island.class, WoodlandChangeling.class})
class DeepSeaSerpentTest extends BaseCardTest {

    // ===== Attack restriction =====

    @Test
    @DisplayName("Deep-Sea Serpent can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Island());

        addCreatureReady(player1, new DeepSeaSerpent());
        declareAttackers(List.of(0));

        // Combat auto-advances; 5/5 unblocked deals 5 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Deep-Sea Serpent cannot attack when defending player does not control an Island")
    void cannotAttackWhenDefenderDoesNotControlIsland() {
        addCreatureReady(player1, new DeepSeaSerpent());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deep-Sea Serpent cannot attack if defender controls only a changeling creature")
    void cannotAttackWhenDefenderOnlyControlsChangelingCreature() {
        harness.addToBattlefield(player2, new WoodlandChangeling());

        addCreatureReady(player1, new DeepSeaSerpent());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Combat damage =====

    @Test
    @DisplayName("Unblocked Deep-Sea Serpent deals 5 damage to defending player")
    void dealsFiveDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent serpent = addCreatureReady(player1, new DeepSeaSerpent());
        serpent.setAttacking(true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("A tapped Island satisfies the attack restriction")
    void canAttackWhenDefendersIslandIsTapped() {
        harness.setLife(player2, 20);
        harness.addToBattlefieldAndReturn(player2, new Island()).tap();
        addCreatureReady(player1, new DeepSeaSerpent());

        declareAttackers(List.of(0));

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("An Island controlled only by the attacker does not permit attacking")
    void cannotAttackWhenOnlyAttackerControlsIsland() {
        addCreatureReady(player1, new DeepSeaSerpent());
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An Island in the defending player's graveyard does not permit attacking")
    void cannotAttackWhenIslandIsOnlyInDefendersGraveyard() {
        harness.setGraveyard(player2, List.of(new Island()));
        addCreatureReady(player1, new DeepSeaSerpent());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deep-Sea Serpent can block when the attacking player controls no Island")
    void canBlockWithoutAttackingPlayersIsland() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DeepSeaSerpent());
        addCreatureReady(player2, new DeepSeaSerpent());
        harness.addToBattlefield(player2, new Island());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Deep-Sea Serpent");
        harness.assertInGraveyard(player2, "Deep-Sea Serpent");
    }
}
