package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({PlatedCrusher.class, GrizzlyBears.class, Shock.class})
class PlatedCrusherTest extends BaseCardTest {

    @Test
    @DisplayName("Its controller can target Plated Crusher despite hexproof")
    void controllerCanTargetWithSpells() {
        Permanent crusher = addCrusherReady(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, crusher.getId());

        assertThat(crusher.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Plated Crusher");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Opponent cannot target Plated Crusher with spells")
    void opponentCannotTargetWithSpells() {
        Permanent crusher = addCrusherReady(player1);

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, crusher.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessCombatDamage() {
        harness.setLife(player2, 20);

        Permanent crusher = addCrusherReady(player1);
        crusher.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 5
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(crusher);
    }

    @Test
    @DisplayName("Trample requires lethal damage to a blocker before damage to the player")
    void trampleRequiresLethalDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent crusher = addCrusherReady(player1);
        crusher.setAttacking(true);
        Permanent blocker = addCrusherReady(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 5,
                player2.getId(), 2
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trample");

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 6,
                player2.getId(), 1
        ));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Plated Crusher");
        harness.assertInGraveyard(player2, "Plated Crusher");
    }

    private Permanent addCrusherReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new PlatedCrusher());
    }
}
