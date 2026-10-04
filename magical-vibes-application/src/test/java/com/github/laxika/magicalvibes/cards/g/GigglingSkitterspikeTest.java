package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BraveTheSands;
import com.github.laxika.magicalvibes.cards.s.SearingTouch;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GigglingSkitterspike.class, GrizzlyBears.class, SearingTouch.class, BraveTheSands.class})
class GigglingSkitterspikeTest extends BaseCardTest {

    @Test
    void attacksAndDealsDamageEqualToItsPowerToEachOpponent() {
        addCreatureReady(player1, new GigglingSkitterspike());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }

    @Test
    void blocksAndDealsDamageEqualToItsPowerToEachOpponent() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GigglingSkitterspike());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 19);
    }

    @Test
    void becomesTheTargetOfASpellAndDealsDamageToEachOpponent() {
        Permanent skitterspike = addCreatureReady(player1, new GigglingSkitterspike());
        harness.setHand(player2, List.of(new SearingTouch()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, skitterspike.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(skitterspike.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void monstrosityPutsFiveCountersOnItAndUpdatesItsDamage() {
        addCreatureReady(player1, new GigglingSkitterspike());
        Permanent skitterspike = gd.playerBattlefields.get(player1.getId()).get(0);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(skitterspike.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(skitterspike.isMonstrous()).isTrue();

        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player2, 14);
    }

    @Test
    void canActivateMonstrosityAgainButGetsNoAdditionalCounters() {
        Permanent skitterspike = addCreatureReady(player1, new GigglingSkitterspike());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(skitterspike.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(skitterspike.isMonstrous()).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void twoPendingMonstrosityActivationsOnlyAddCountersOnce() {
        Permanent skitterspike = addCreatureReady(player1, new GigglingSkitterspike());
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(skitterspike.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(skitterspike.isMonstrous()).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void ownSpellTriggersDamageUsingPowerAtResolution() {
        Permanent skitterspike = addCreatureReady(player1, new GigglingSkitterspike());
        harness.setHand(player1, List.of(new SearingTouch()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstant(player1, 0, skitterspike.getId());
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 14);
        assertThat(skitterspike.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Giggling Skitterspike");
    }

    @Test
    @CardUsed({GigglingSkitterspike.class, GrizzlyBears.class, BraveTheSands.class})
    void blockingTwoCreaturesTriggersDamageOnlyOnce() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GigglingSkitterspike());
        harness.addToBattlefield(player2, new BraveTheSands());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Giggling Skitterspike");
    }
}
