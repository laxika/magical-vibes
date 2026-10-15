package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CourtHomunculus;
import com.github.laxika.magicalvibes.cards.p.PathToExile;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InkwellLeviathan.class, CourtHomunculus.class, PathToExile.class, Island.class})
class InkwellLeviathanTest extends BaseCardTest {

    @Test
    void defenderIslandPreventsBlocking() {
        addCreatureReady(player1, new InkwellLeviathan());
        Permanent blocker = addCreatureReady(player2, new CourtHomunculus());
        harness.addToBattlefield(player2, new Island());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    void attackersIslandDoesNotPreventBlocking() {
        addCreatureReady(player1, new InkwellLeviathan());
        harness.addToBattlefield(player1, new Island());
        Permanent blocker = addCreatureReady(player2, new CourtHomunculus());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void trampleDealsExcessDamageToDefender() {
        Permanent attacker = addCreatureReady(player1, new InkwellLeviathan());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CourtHomunculus());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 6));

        harness.assertLife(player2, 14);
        harness.assertInGraveyard(player2, "Court Homunculus");
        harness.assertOnBattlefield(player1, "Inkwell Leviathan");
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void trampleRequiresLethalDamageToBlockerBeforeOverflow() {
        Permanent attacker = addCreatureReady(player1, new InkwellLeviathan());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CourtHomunculus());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(player2.getId(), 7)))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 6));
        harness.assertLife(player2, 14);
    }

    @Test
    void shroudPreventsOpponentTargeting() {
        Permanent leviathan = addCreatureReady(player2, new InkwellLeviathan());
        harness.setHand(player1, List.of(new PathToExile()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, leviathan.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
        harness.assertOnBattlefield(player2, "Inkwell Leviathan");
    }

    @Test
    void shroudPreventsControllerTargeting() {
        Permanent leviathan = addCreatureReady(player1, new InkwellLeviathan());
        harness.setHand(player1, List.of(new PathToExile()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, leviathan.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
        harness.assertOnBattlefield(player1, "Inkwell Leviathan");
    }
}
