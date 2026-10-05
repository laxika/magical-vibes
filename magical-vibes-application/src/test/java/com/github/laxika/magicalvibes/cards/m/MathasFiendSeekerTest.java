package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MathasFiendSeeker.class, Forest.class, GrizzlyBears.class})
class MathasFiendSeekerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a bounty counter on an opponent's creature at your end step")
    void putsBountyCounterOnTargetOpponentCreatureAtYourEndStep() {
        castMathas();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.BOUNTY)).isOne();
    }

    @Test
    void bountiedCreatureRewardsEachOpponentWhenItDiesAfterMathasLeaves() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent mathas = castMathas();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        putBountyOn(bears);

        gd.playerBattlefields.get(player1.getId()).remove(mathas);
        gd.expireFloatingEffectsForDepartedSource(mathas.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void unbountiedCreatureDoesNotRewardOpponentsWhenItDies() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        castMathas();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bears));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent castMathas() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new MathasFiendSeeker(), "{R}{W}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Mathas, Fiend Seeker");
    }

    @Test
    void separateResolutionsGrantSeparateDeathAbilities() {
        castMathas();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        putBountyOn(bears);
        putBountyOn(bears);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }

    @Test
    void bountyCounterNotPlacedByMathasDoesNotGrantDeathAbility() {
        castMathas();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.BOUNTY, 1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bears));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void replacingLastBountyCounterDoesNotRestoreExpiredDeathAbility() {
        castMathas();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        putBountyOn(bears);
        bears.setCounterCount(CounterType.BOUNTY, 0);
        harness.runStateBasedActions();
        bears.setCounterCount(CounterType.BOUNTY, 1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bears));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }

    private void putBountyOn(Permanent creature) {
        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
    }

    @Test
    void doesNotTriggerAtOpponentsEndStep() {
        castMathas();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(bears.getCounterCount(CounterType.BOUNTY)).isZero();
    }

    @Test
    void removingLastBountyCounterRemovesDeathAbility() {
        castMathas();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        putBountyOn(bears);
        bears.setCounterCount(CounterType.BOUNTY, 0);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bears));

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }
}
