package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.c.ChangelingOutcast;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteamFrigate.class, Island.class, ChangelingOutcast.class})
class SteamFrigateTest extends BaseCardTest {

    @Test
    @DisplayName("Steam Frigate can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Island());

        addCreatureReady(player1, new SteamFrigate());
        declareAttackers(List.of(0));

        // Combat auto-advances; verify attack went through by checking damage dealt (3/3)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Steam Frigate cannot attack when defending player does not control an Island")
    void cannotAttackWhenDefenderDoesNotControlIsland() {
        addCreatureReady(player1, new SteamFrigate());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Steam Frigate cannot attack when only its controller controls an Island")
    void cannotAttackWhenOnlyAttackingPlayerControlsIsland() {
        addCreatureReady(player1, new SteamFrigate());
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Steam Frigate cannot attack if defender controls only a changeling creature")
    void cannotAttackWhenDefenderOnlyControlsChangelingCreature() {
        addCreatureReady(player2, new ChangelingOutcast());
        addCreatureReady(player1, new SteamFrigate());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("A tapped Island still allows Steam Frigate to attack")
    void canAttackWhenDefendersIslandIsTapped() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        island.tap();
        addCreatureReady(player1, new SteamFrigate());

        declareAttackers(List.of(0));

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("An Island in the graveyard does not allow Steam Frigate to attack")
    void cannotAttackWhenIslandIsOnlyInGraveyard() {
        harness.setGraveyard(player2, List.of(new Island()));
        addCreatureReady(player1, new SteamFrigate());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Steam Frigate can block an opponent who controls no Island")
    void canBlockWhenAttackingPlayerControlsNoIsland() {
        addCreatureReady(player1, new SteamFrigate());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SteamFrigate());
        harness.addToBattlefield(player2, new Island());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        resolveCombat();
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Steam Frigate");
        harness.assertInGraveyard(player2, "Steam Frigate");
    }
}
