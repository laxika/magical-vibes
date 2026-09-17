package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.*;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class GameRegistrySessionTest {
    @Test
    void playerRoutingFollowsTheActiveFrameButLobbyListsOnlyTheRoot() {
        GameRegistry registry = new GameRegistry();
        UUID player = UUID.randomUUID();
        GameData root = new GameData(UUID.randomUUID(), "Game", player, "Player");
        root.status = GameStatus.RUNNING;
        root.playerIds.add(player);
        registry.register(root);
        GameContext initial = root.session.context();
        GameData child = new GameData(UUID.randomUUID(), "Game", player, "Player");
        child.status = GameStatus.MULLIGAN;
        child.playerIds.add(player);
        root.session.push(child);
        registry.register(child);
        assertThat(registry.getGameForPlayer(player)).isSameAs(child);
        assertThat(registry.get(root.id)).isSameAs(root);
        assertThat(registry.getRunningGames()).containsExactly(root);
        root.session.pop();
        registry.remove(child.id);
        assertThat(registry.getGameForPlayer(player)).isSameAs(root);
        assertThat(root.session.context().activationEpoch()).isGreaterThan(initial.activationEpoch());
        assertThat(root.session.context()).isNotEqualTo(initial);
    }

    @Test
    void removingASessionReleasesAllItsFrames() {
        GameRegistry registry = new GameRegistry();
        UUID player = UUID.randomUUID();
        GameData root = new GameData(UUID.randomUUID(), "Game", player, "Player");
        GameData child = new GameData(UUID.randomUUID(), "Game", player, "Player");
        registry.register(root);
        root.session.push(child);
        registry.register(child);
        registry.remove(root.id);
        assertThat(registry.get(root.id)).isNull();
        assertThat(registry.get(child.id)).isNull();
    }

    @Test
    void copyingARolloutAfterItsStartingChildEndsCopiesTheResumedGame() {
        UUID player = UUID.randomUUID();
        GameData root = new GameData(UUID.randomUUID(), "Game", player, "Player");
        GameData child = new GameData(UUID.randomUUID(), "Game", player, "Player");
        root.session.push(child);
        root.session.pop();
        GameData copy = child.simulationCopy();
        assertThat(copy).isNotNull().isNotSameAs(root);
        assertThat(copy.id).isEqualTo(root.id);
        assertThat(copy.session.active()).isSameAs(copy);
        assertThat(copy.simulation).isTrue();
    }
    @Test
    void restoringASimulatedTurnDoesNotMutateTheLiveSnapshotOrReplaceItsSession() {
        UUID player = UUID.randomUUID();
        GameData root = new GameData(UUID.randomUUID(), "Game", player, "Player");
        GameData child = new GameData(UUID.randomUUID(), "Game", player, "Player");
        root.session.push(child);
        child.playerLifeTotals.put(player, 20);
        child.captureTurnStartSnapshot();
        GameData first = child.simulationCopy();
        assertThat(first.restoreTurnStartSnapshot()).isTrue();
        assertThat(first.session.active()).isSameAs(first);
        first.playerLifeTotals.put(player, 3);
        GameData second = child.simulationCopy();
        assertThat(second.restoreTurnStartSnapshot()).isTrue();
        assertThat(second.playerLifeTotals.get(player)).isEqualTo(20);
        GameSession liveSession = child.session;
        assertThat(child.restoreTurnStartSnapshot()).isTrue();
        assertThat(child.session).isSameAs(liveSession);
        assertThat(child.session.depth()).isEqualTo(1);
        assertThat(child.playerLifeTotals.get(player)).isEqualTo(20);
    }

    @Test
    void pausedCombatDamageUsesTheCopiedPermanentsAndIndependentAssignments() {
        UUID player = UUID.randomUUID();
        GameData root = new GameData(UUID.randomUUID(), "Game", player, "Player");
        Permanent permanent = new Permanent(new Card());
        root.playerBattlefields.put(player, new java.util.ArrayList<>(java.util.List.of(permanent)));
        root.pendingCombatDamageState = new CombatDamageState();
        root.pendingCombatDamageState.combatDamageAmountsToCreatures.put(permanent,
                new java.util.HashMap<>(java.util.Map.of(permanent.getId(), 2)));
        root.pendingCombatDamageRedirectTarget = permanent;
        GameData copy = root.simulationCopy();
        Permanent copiedPermanent = copy.playerBattlefields.get(player).getFirst();
        assertThat(copy.pendingCombatDamageRedirectTarget).isSameAs(copiedPermanent).isNotSameAs(permanent);
        copy.pendingCombatDamageState.combatDamageAmountsToCreatures.get(copiedPermanent).clear();
        assertThat(root.pendingCombatDamageState.combatDamageAmountsToCreatures.get(permanent)).hasSize(1);
    }
}
