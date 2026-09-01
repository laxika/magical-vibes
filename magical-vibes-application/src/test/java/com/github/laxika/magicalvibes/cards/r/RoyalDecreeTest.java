package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        RoyalDecree.class,
        DrudgeSkeletons.class,
        Forest.class,
        GrizzlyBears.class,
        Mountain.class,
        Plains.class,
        RagingGoblin.class,
        Swamp.class
})
class RoyalDecreeTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's Swamp becoming tapped deals 1 damage to its controller")
    void opponentSwampTapDamagesController() {
        harness.addToBattlefield(player1, new RoyalDecree());
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setLife(player2, 20);

        tap(swamp);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Own Mountain becoming tapped deals 1 damage to yourself")
    void ownMountainTapDamagesSelf() {
        harness.addToBattlefield(player1, new RoyalDecree());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setLife(player1, 20);

        tap(mountain);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Black creature becoming tapped deals 1 damage to its controller")
    void blackCreatureTapDamagesController() {
        harness.addToBattlefield(player1, new RoyalDecree());
        Permanent skeletons = harness.addToBattlefieldAndReturn(player2, new DrudgeSkeletons());
        harness.setLife(player2, 20);

        tap(skeletons);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Red creature becoming tapped deals 1 damage to its controller")
    void redCreatureTapDamagesController() {
        harness.addToBattlefield(player1, new RoyalDecree());
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        harness.setLife(player2, 20);

        tap(goblin);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Tapping a Plains does not trigger")
    void plainsDoesNotTrigger() {
        harness.addToBattlefield(player1, new RoyalDecree());
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setLife(player2, 20);

        tap(plains);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Tapping a green creature does not trigger")
    void greenCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new RoyalDecree());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        tap(bears);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Tapping a Forest does not trigger")
    void forestDoesNotTrigger() {
        harness.addToBattlefield(player1, new RoyalDecree());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setLife(player2, 20);

        tap(forest);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("One trigger per tap (not one per matching criterion)")
    void oneTriggerPerTap() {
        harness.addToBattlefield(player1, new RoyalDecree());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setLife(player2, 20);

        tap(mountain);

        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Royal Decree")
    void paysCumulativeUpkeep() {
        Permanent decree = harness.addToBattlefieldAndReturn(player1, new RoyalDecree());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(decree.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(decree);
    }

    @Test
    @DisplayName("Cumulative upkeep costs one white mana per age counter")
    void paysCumulativeUpkeepForEachAgeCounter() {
        Permanent decree = harness.addToBattlefieldAndReturn(player1, new RoyalDecree());
        decree.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(decree.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(decree);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Royal Decree")
    void declineSacrifices() {
        Permanent decree = harness.addToBattlefieldAndReturn(player1, new RoyalDecree());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(decree);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(decree.getCard());
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
