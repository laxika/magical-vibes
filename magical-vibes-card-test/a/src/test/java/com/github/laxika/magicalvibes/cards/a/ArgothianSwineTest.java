package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.PouncingJaguar;
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

@CardUsed({ArgothianSwine.class, PouncingJaguar.class})
class ArgothianSwineTest extends BaseCardTest {

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessDamageToDefendingPlayer() {
        harness.setLife(player2, 20);

        Permanent swine = addCreatureReady(player1, new ArgothianSwine());
        swine.setAttacking(true);

        Permanent jaguar = addCreatureReady(player2, new PouncingJaguar());

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                jaguar.getId(), 2,
                player2.getId(), 1
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertOnBattlefield(player1, "Argothian Swine");
        harness.assertInGraveyard(player2, "Pouncing Jaguar");
    }

    @Test
    @DisplayName("Trample may assign all damage to the blocker")
    void mayAssignAllDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent swine = addCreatureReady(player1, new ArgothianSwine());
        swine.setAttacking(true);
        Permanent jaguar = addCreatureReady(player2, new PouncingJaguar());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(jaguar.getId(), 3));

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Argothian Swine");
        harness.assertInGraveyard(player2, "Pouncing Jaguar");
    }

    @Test
    @DisplayName("Trample requires lethal damage to a blocker before damaging the player")
    void rejectsPlayerDamageBeforeLethalDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent swine = addCreatureReady(player1, new ArgothianSwine());
        swine.setAttacking(true);
        Permanent jaguar = addCreatureReady(player2, new PouncingJaguar());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(jaguar.getId(), 1, player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Pouncing Jaguar");

        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(jaguar.getId(), 2, player2.getId(), 1));
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Pouncing Jaguar");
    }

    @Test
    @DisplayName("Trample cannot damage the player unless every blocker is assigned lethal damage")
    void requiresLethalDamageToEveryBlocker() {
        harness.setLife(player2, 20);
        Permanent swine = addCreatureReady(player1, new ArgothianSwine());
        swine.setAttacking(true);
        Permanent firstJaguar = addCreatureReady(player2, new PouncingJaguar());
        Permanent secondJaguar = addCreatureReady(player2, new PouncingJaguar());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(firstJaguar.getId(), 2, player2.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);

        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(firstJaguar.getId(), 2, secondJaguar.getId(), 1));
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Argothian Swine");
        harness.assertInGraveyard(player2, "Pouncing Jaguar");
        assertThat(countPermanents(player2, "Pouncing Jaguar")).isEqualTo(1);
    }
}
