package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AdiposeOffspring;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VashtaNerada.class, AdiposeOffspring.class})
class VashtaNeradaTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter at each end step when a creature died this turn")
    void getsCounterAtEachEndStepWhenCreatureDied() {
        Permanent vashtaNerada = harness.addToBattlefieldAndReturn(player1, new VashtaNerada());
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(vashtaNerada.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a counter when no creature died this turn")
    void doesNotGetCounterWithoutCreatureDeath() {
        Permanent vashtaNerada = harness.addToBattlefieldAndReturn(player1, new VashtaNerada());

        advanceToEndStep(player1);

        assertThat(vashtaNerada.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    @Test
    void getsOnlyOneCounterForMultipleActualDeathsOnItsControllersTurn() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new VashtaNerada());
        Permanent firstDeath = harness.addToBattlefieldAndReturn(player1, new VashtaNerada());
        Permanent secondDeath = harness.addToBattlefieldAndReturn(player2, new VashtaNerada());

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, firstDeath);
            harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, secondDeath);
        });
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstDeath.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(secondDeath.getCard());
    }

    @Test
    void deathAfterEndStepBeginsDoesNotRetroactivelyTriggerMorbid() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new VashtaNerada());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new VashtaNerada());
        advanceToEndStep(player2);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, victim));

        assertThat(gd.stack).isEmpty();
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(victim.getCard());
    }

    @Test
    void lethalDamageDoesNotKillItOrEnableMorbid() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new VashtaNerada());
        creature.setMarkedDamage(10);
        harness.runStateBasedActions();

        advanceToEndStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void shadowAllowsOnlyShadowCreaturesToBlockInEitherDirection() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new VashtaNerada());
        Permanent shadowBlocker = harness.addToBattlefieldAndReturn(player2, new VashtaNerada());
        Permanent ordinaryBlocker = harness.addToBattlefieldAndReturn(player2, new AdiposeOffspring());
        Permanent ordinaryAttacker = harness.addToBattlefieldAndReturn(player1, new AdiposeOffspring());

        assertThat(harness.getBlockLegalityService().canBlockAttacker(gd, shadowBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
        assertThat(harness.getBlockLegalityService().canBlockAttacker(gd, ordinaryBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(harness.getBlockLegalityService().canBlockAttacker(gd, shadowBlocker, ordinaryAttacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }
}
