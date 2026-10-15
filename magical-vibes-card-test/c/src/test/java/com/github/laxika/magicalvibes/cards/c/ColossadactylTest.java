package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EkunduGriffin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({Colossadactyl.class, EkunduGriffin.class, GrizzlyBears.class})
class ColossadactylTest extends BaseCardTest {

    @Test
    @DisplayName("Reach allows Colossadactyl to block a flying creature")
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new EkunduGriffin());
        Permanent colossadactyl = addCreatureReady(player2, new Colossadactyl());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(colossadactyl.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessDamageToPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new Colossadactyl());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Trample cannot assign damage to the player before assigning lethal damage to the blocker")
    void trampleRequiresLethalDamageToBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new Colossadactyl());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 3)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trample");

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2));
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Trample accounts for damage already marked on the blocker")
    void trampleAccountsForMarkedDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new Colossadactyl());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setMarkedDamage(1);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 3));

        harness.assertLife(player2, 17);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Trample allows assigning all damage to the blocker")
    void trampleDoesNotRequireDamageToPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new Colossadactyl());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 4));

        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }
}
