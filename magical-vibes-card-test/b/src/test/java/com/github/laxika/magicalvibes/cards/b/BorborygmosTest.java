package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GruulNodorog;
import com.github.laxika.magicalvibes.cards.g.GruulScrapper;
import com.github.laxika.magicalvibes.cards.g.GruulSignet;
import com.github.laxika.magicalvibes.cards.r.Repeal;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({Borborygmos.class, GruulNodorog.class, GruulScrapper.class, GruulSignet.class, Repeal.class})
class BorborygmosTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each creature its controller controls after combat damage")
    void putsCountersOnControlledCreaturesAfterCombatDamage() {
        Permanent borborygmos = addCreatureReady(player1, new Borborygmos());
        Permanent ownCreature = addCreatureReady(player1, new GruulScrapper());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new GruulSignet());
        Permanent opposingCreature = addCreatureReady(player2, new GruulScrapper());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(borborygmos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownArtifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when a blocker takes all combat damage")
    void doesNotTriggerWithoutCombatDamageToPlayer() {
        Permanent borborygmos = addCreatureReady(player1, new Borborygmos());
        Permanent blocker = addCreatureReady(player2, new Borborygmos());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 6));

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(borborygmos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Triggers when trample deals excess combat damage to a player")
    void triggersForTrampleDamageToPlayer() {
        Permanent borborygmos = addCreatureReady(player1, new Borborygmos());
        Permanent blocker = addCreatureReady(player2, new GruulNodorog());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 4, player2.getId(), 2));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(borborygmos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Trigger survives its source leaving and includes creatures present at resolution")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        Permanent borborygmos = addCreatureReady(player1, new Borborygmos());
        Permanent ownCreature = addCreatureReady(player1, new GruulScrapper());
        Permanent opposingCreature = addCreatureReady(player2, new GruulScrapper());
        harness.setHand(player1, List.of(new Repeal()));
        harness.setLibrary(player1, List.of(new GruulScrapper()));

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.COMBAT_DAMAGE, () -> {
            declareAttackersAndPrepareBlockers(List.of(0));
            gs.declareBlockers(gd, player2, List.of());
        });

        assertThat(gd.stack).hasSize(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.castInstant(player1, 0, 7, borborygmos.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Borborygmos");
        harness.assertNotOnBattlefield(player1, "Borborygmos");

        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new GruulScrapper());
        resolveAllTriggers();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lateCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(borborygmos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
