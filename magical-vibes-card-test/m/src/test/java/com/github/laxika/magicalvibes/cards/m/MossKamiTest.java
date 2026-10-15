package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HearthKami;
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

@CardUsed({MossKami.class, HearthKami.class})
class MossKamiTest extends BaseCardTest {

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MossKami());
        Permanent blocker = addCreatureReady(player2, new HearthKami());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 4
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
    @Test
    @DisplayName("Trample requires lethal damage to the blocker before player damage")
    void cannotTrampleOverBlockerWithoutAssigningLethalDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MossKami());
        Permanent blocker = addCreatureReady(player2, new HearthKami());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(player2.getId(), 5)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trample: must assign");
        harness.assertLife(player2, 20);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 4
        ));
        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player2, "Hearth Kami");
    }

    @Test
    @DisplayName("Trample may assign all combat damage to the blocker")
    void canAssignAllDamageToBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MossKami());
        Permanent blocker = addCreatureReady(player2, new HearthKami());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 5));

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Hearth Kami");
        harness.assertOnBattlefield(player1, "Moss Kami");
    }

    @Test
    @DisplayName("Trample assigns lethal damage to each blocker before excess player damage")
    void trampleOverMultipleBlockers() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MossKami());
        Permanent firstBlocker = addCreatureReady(player2, new HearthKami());
        Permanent secondBlocker = addCreatureReady(player2, new HearthKami());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 1,
                player2.getId(), 4
        ))).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trample: must assign");

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 1,
                secondBlocker.getId(), 1,
                player2.getId(), 3
        ));

        harness.assertLife(player2, 17);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Moss Kami");
    }
}
