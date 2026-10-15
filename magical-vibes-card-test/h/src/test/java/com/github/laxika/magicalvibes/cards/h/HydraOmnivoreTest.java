package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HydraOmnivore.class})
class HydraOmnivoreTest extends BaseCardTest {

    @Test
    @DisplayName("In a two-player game, combat damage is not dealt to the same opponent again")
    void doesNotDamageTheDamagedOpponentAgain() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new HydraOmnivore());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
    }

    @Test
    void damagesEveryOtherOpponentWithoutDamagingControllerOrRepeatingCombatDamage() {
        Player third = addOpponent("Charlie");
        Player fourth = addOpponent("Dana");
        dealCombatDamage();

        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
        assertThat(gd.getLife(third.getId())).isEqualTo(20);
        assertThat(gd.getLife(fourth.getId())).isEqualTo(20);

        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
        assertThat(gd.getLife(third.getId())).isEqualTo(12);
        assertThat(gd.getLife(fourth.getId())).isEqualTo(12);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void usesDamageDealtRatherThanPowerAtResolution() {
        Player third = addOpponent("Charlie");
        Permanent hydra = dealCombatDamage();
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
        assertThat(gd.getLife(third.getId())).isEqualTo(12);
    }

    @Test
    void resolvesAfterSourceLeavesBattlefield() {
        Player third = addOpponent("Charlie");
        Permanent hydra = dealCombatDamage();
        gd.playerBattlefields.get(player1.getId()).remove(hydra);
        gd.playerGraveyards.get(player1.getId()).add(hydra.getCard());

        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
        assertThat(gd.getLife(third.getId())).isEqualTo(12);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void dealingCombatDamageOnlyToCreatureDoesNotTriggerOpponentDamage() {
        Player third = addOpponent("Charlie");
        addCreatureReady(player1, new HydraOmnivore());
        addCreatureReady(player2, new HydraOmnivore());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.getLife(third.getId())).isEqualTo(20);
    }

    private Permanent dealCombatDamage() {
        Permanent hydra = addCreatureReady(player1, new HydraOmnivore());
        hydra.setAttacking(true);
        hydra.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        return hydra;
    }

    private Player addOpponent(String name) {
        UUID id = UUID.randomUUID();
        Player opponent = new Player(id, name);
        gd.playerIds.add(id);
        gd.orderedPlayerIds.add(id);
        gd.playerNames.add(name);
        gd.playerIdToName.put(id, name);
        gd.playerDecks.put(id, new ArrayList<>());
        gd.playerHands.put(id, new ArrayList<>());
        gd.playerBattlefields.put(id, new ArrayList<>());
        gd.playerGraveyards.put(id, new ArrayList<>());
        gd.playerCommandZones.put(id, new ArrayList<>());
        gd.playerManaPools.put(id, new ManaPool());
        gd.playerLifeTotals.put(id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-" + name), id, name);
        return opponent;
    }
}
