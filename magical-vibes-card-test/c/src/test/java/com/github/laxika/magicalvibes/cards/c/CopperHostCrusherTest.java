package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.p.PortentTracker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CopperHostCrusher.class, Shock.class, PortentTracker.class})
class CopperHostCrusherTest extends BaseCardTest {

    @Test
    @DisplayName("Trample requires lethal damage before assigning excess to the defender")
    void tramplesOverBlockerAfterAssigningLethalDamage() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        addCreatureReady(player1, new CopperHostCrusher());
        Permanent blocker = addCreatureReady(player2, new PortentTracker());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(player2.getId(), 8)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);

        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 7));
        harness.assertLife(player2, 13);
        harness.assertInGraveyard(player2, "Portent Tracker");
        harness.assertOnBattlefield(player1, "Copper Host Crusher");
    }

    @Test
    @DisplayName("Opponent cannot target Copper Host Crusher with spells")
    void opponentCannotTargetWithSpells() {
        Permanent crusher = addCreatureReady(player1, new CopperHostCrusher());

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, crusher.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Hexproof allows the controller to target Copper Host Crusher")
    void controllerCanTargetWithSpells() {
        Permanent crusher = addCreatureReady(player1, new CopperHostCrusher());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, crusher.getId());

        assertThat(crusher.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Copper Host Crusher");
        harness.assertInGraveyard(player1, "Shock");
    }
}
