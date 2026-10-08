package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TymaretTheMurderKing;
import com.github.laxika.magicalvibes.cards.t.TheOzolith;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YoshimaruEverFaithful.class, TheOzolith.class, GrizzlyBears.class,
        TymaretTheMurderKing.class})
class YoshimaruEverFaithfulTest extends BaseCardTest {

    @Test
    void doesNotTriggerFromItsOwnEntry() {
        harness.castFromHand(player1, new YoshimaruEverFaithful(), "{W}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Yoshimaru, Ever Faithful")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void putsCounterOnEntryOfLegendaryNoncreaturePermanentYouControl() {
        Permanent yoshimaru = harness.addToBattlefieldAndReturn(player1, new YoshimaruEverFaithful());

        harness.enterBattlefieldAndReturn(player1, new TheOzolith());
        harness.passBothPriorities();

        assertThat(yoshimaru.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForNonlegendaryPermanent() {
        Permanent yoshimaru = harness.addToBattlefieldAndReturn(player1, new YoshimaruEverFaithful());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(yoshimaru.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerForLegendaryPermanentAnOpponentControls() {
        Permanent yoshimaru = harness.addToBattlefieldAndReturn(player1, new YoshimaruEverFaithful());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new TymaretTheMurderKing(), "{B}{R}");
        harness.passBothPriorities();

        assertThat(yoshimaru.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void legendaryCreatureEntryQueuesCounterUntilAbilityResolves() {
        Permanent yoshimaru = harness.addToBattlefieldAndReturn(player1, new YoshimaruEverFaithful());

        harness.enterBattlefieldAndReturn(player1, new TymaretTheMurderKing());

        assertThat(yoshimaru.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        resolveAllTriggers();
        assertThat(yoshimaru.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void eachLegendaryPermanentEntryAddsOneCounter() {
        Permanent yoshimaru = harness.addToBattlefieldAndReturn(player1, new YoshimaruEverFaithful());

        harness.enterBattlefieldAndReturn(player1, new TheOzolith());
        harness.enterBattlefieldAndReturn(player1, new TymaretTheMurderKing());
        resolveAllTriggers();

        assertThat(yoshimaru.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void counterIsAddedEvenIfEnteringLegendaryPermanentLeavesBeforeResolution() {
        Permanent yoshimaru = harness.addToBattlefieldAndReturn(player1, new YoshimaruEverFaithful());
        Permanent tymaret = harness.enterBattlefieldAndReturn(player1, new TymaretTheMurderKing());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, tymaret));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Tymaret, the Murder King");
        assertThat(yoshimaru.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void pendingAbilityDoesNotPutCounterOnYoshimaruAfterItLeavesAndReturns() {
        YoshimaruEverFaithful card = new YoshimaruEverFaithful();
        Permanent original = harness.addToBattlefieldAndReturn(player1, card);
        harness.enterBattlefieldAndReturn(player1, new TymaretTheMurderKing());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, original));
        gd.removeFromExile(card.getId());
        Permanent returned = harness.enterBattlefieldAndReturn(player1, card);
        resolveAllTriggers();

        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
