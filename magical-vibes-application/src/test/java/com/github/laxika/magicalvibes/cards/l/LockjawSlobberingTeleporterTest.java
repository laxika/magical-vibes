package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RapidHybridization;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LockjawSlobberingTeleporter.class, BurstOfStrength.class, GrizzlyBears.class,
        RapidHybridization.class})
class LockjawSlobberingTeleporterTest extends BaseCardTest {

    @Test
    void castsNoncreatureSpellThenGainsCounterAndMakesTwoCreaturesUnblockable() {
        Permanent lockjaw = harness.addToBattlefieldAndReturn(player1, new LockjawSlobberingTeleporter());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, otherCreature.getId());

        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).containsExactly(otherCreature.getId());
        assertThat(targetChoice.validPlayerIds()).contains(player1.getId());
        assertThat(targetChoice.validPermanentIds()).doesNotContain(lockjaw.getId(), opposingCreature.getId());

        harness.handlePermanentChosen(player1, otherCreature.getId());
        harness.passBothPriorities();

        assertThat(lockjaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, lockjaw)).isTrue();
        assertThat(otherCreature.isCantBeBlocked()).isTrue();
        assertThat(opposingCreature.isCantBeBlocked()).isFalse();
    }

    @Test
    void doesNotTriggerWithoutCastingANoncreatureSpell() {
        Permanent lockjaw = harness.addToBattlefieldAndReturn(player1, new LockjawSlobberingTeleporter());

        advanceToBeginningOfCombat();

        assertThat(lockjaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(lockjaw.isCantBeBlocked()).isFalse();
    }

    @Test
    void canDeclineOtherTargetAndUnblockabilityExpiresAtEndOfTurn() {
        Permanent lockjaw = harness.addToBattlefieldAndReturn(player1, new LockjawSlobberingTeleporter());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, otherCreature.getId());

        advanceToBeginningOfCombat();
        assertThat(lockjaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(lockjaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, lockjaw)).isFalse();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, lockjaw)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, otherCreature)).isFalse();
        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gqs.hasCantBeBlocked(gd, lockjaw)).isFalse();
        assertThat(lockjaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void castingOnlyACreatureSpellDoesNotTrigger() {
        Permanent lockjaw = harness.addToBattlefieldAndReturn(player1, new LockjawSlobberingTeleporter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        advanceToBeginningOfCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(lockjaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasCantBeBlocked(gd, lockjaw)).isFalse();
    }

    @Test
    void noncreatureSpellCastDuringBeginningOfCombatIsTooLate() {
        Permanent lockjaw = harness.addToBattlefieldAndReturn(player1, new LockjawSlobberingTeleporter());
        advanceToBeginningOfCombat();
        assertThat(gd.stack).isEmpty();
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, lockjaw.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(lockjaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, lockjaw)).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void illegalOtherTargetPreventsLockjawFromBecomingUnblockable() {
        Permanent lockjaw = harness.addToBattlefieldAndReturn(player1, new LockjawSlobberingTeleporter());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, otherCreature.getId());
        advanceToBeginningOfCombat();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, otherCreature.getId());

        harness.setHand(player2, List.of(new RapidHybridization()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, otherCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(otherCreature);
        assertThat(lockjaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, lockjaw)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removingLockjawAfterCounterPlacementDoesNotStopOtherCreatureBecomingUnblockable() {
        Permanent lockjaw = harness.addToBattlefieldAndReturn(player1, new LockjawSlobberingTeleporter());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, otherCreature.getId());
        advanceToBeginningOfCombat();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, otherCreature.getId());

        harness.setHand(player2, List.of(new RapidHybridization()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, lockjaw.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lockjaw);
        assertThat(gqs.hasCantBeBlocked(gd, otherCreature)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsCombatEvenAfterCastingANoncreatureSpell() {
        Permanent lockjaw = harness.addToBattlefieldAndReturn(player1, new LockjawSlobberingTeleporter());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, lockjaw.getId());

        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(lockjaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, lockjaw)).isFalse();
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
