package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderwolfCavalry.class, AirElemental.class, GrizzlyBears.class, SolRing.class})
class ThunderwolfCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each other creature you control after dealing combat damage")
    void putsCountersOnEachOtherCreatureYouControl() {
        Permanent cavalry = addCreatureReady(player1, new ThunderwolfCavalry());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when it deals no combat damage to a player")
    void doesNotTriggerWithoutCombatDamageToPlayer() {
        Permanent cavalry = addCreatureReady(player1, new ThunderwolfCavalry());
        Permanent ally = addCreatureReady(player1, new ThunderwolfCavalry());
        addCreatureReady(player2, new AirElemental());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Only other controlled creatures receive counters")
    void excludesOpponentsAndNoncreatures() {
        Permanent cavalry = addCreatureReady(player1, new ThunderwolfCavalry());
        Permanent ally = addCreatureReady(player1, new ThunderwolfCavalry());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent opponent = addCreatureReady(player2, new ThunderwolfCavalry());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Each attacking Cavalry puts a counter on the other Cavalries")
    void multipleCavalriesEachExcludeOnlyThemselves() {
        Permanent first = addCreatureReady(player1, new ThunderwolfCavalry());
        Permanent second = addCreatureReady(player1, new ThunderwolfCavalry());
        Permanent nonAttacker = addCreatureReady(player1, new ThunderwolfCavalry());

        declareAttackers(player1, List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Creatures entering before the trigger resolves receive counters")
    void determinesRecipientsAtResolution() {
        Permanent cavalry = addCreatureReady(player1, new ThunderwolfCavalry());
        cavalry.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        Permanent lateArrival = harness.addToBattlefieldAndReturn(player1, new ThunderwolfCavalry());
        assertThat(lateArrival.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        resolveAllTriggers();

        assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(lateArrival.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The combat damage trigger still resolves after Cavalry leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent cavalry = addCreatureReady(player1, new ThunderwolfCavalry());
        Permanent ally = addCreatureReady(player1, new ThunderwolfCavalry());
        cavalry.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, cavalry));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cavalry);
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
