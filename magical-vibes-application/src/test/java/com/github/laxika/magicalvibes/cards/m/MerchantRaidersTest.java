package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CoerciveRecruiter;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.f.FathomFleetBoarder;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerchantRaiders.class, FathomFleetBoarder.class, GrizzlyBears.class,
        CoerciveRecruiter.class, Conspiracy.class})
class MerchantRaidersTest extends BaseCardTest {

    @Test
    @DisplayName("Merchant Raiders taps and locks a creature when it enters")
    void selfEntryTapsAndLocksCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castMerchantRaiders(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getUntapPreventedWhileSourceOnBattlefieldIds()).isNotEmpty();
    }

    @Test
    @DisplayName("A Pirate entering also triggers Merchant Raiders")
    void anotherPirateTriggers() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new MerchantRaiders());

        harness.castFromHand(player1, new FathomFleetBoarder(), "{2}{B}");
        harness.passBothPriorities();
        chooseTargetAndResolve(target);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getUntapPreventedWhileSourceOnBattlefieldIds()).isNotEmpty();
    }

    @Test
    @DisplayName("A non-Pirate entering does not trigger Merchant Raiders")
    void nonPirateDoesNotTrigger() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new MerchantRaiders());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getUntapPreventedWhileSourceOnBattlefieldIds()).isEmpty();
    }

    @Test
    @DisplayName("The trigger may choose no target")
    void mayChooseNoTarget() {
        harness.castFromHand(player1, new MerchantRaiders(), "{3}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof MerchantRaiders);
    }

    @Test
    @DisplayName("The lock ends when Merchant Raiders leaves the battlefield")
    void lockEndsWhenSourceLeavesBattlefield() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castMerchantRaiders(target);
        Permanent merchant = findPermanent(player1, "Merchant Raiders");

        gd.playerBattlefields.get(player1.getId()).remove(merchant);
        advanceToUpkeep(player2);

        assertThat(target.isTapped()).isFalse();
    }

    private void castMerchantRaiders(Permanent target) {
        harness.castFromHand(player1, new MerchantRaiders(), "{3}{U}");
        harness.passBothPriorities();
        chooseTargetAndResolve(target);
    }

    private void chooseTargetAndResolve(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The target stays tapped through its controller's untap step")
    void lockPreventsUntappingWhileSourceControlled() {
        Permanent target = addCreatureReady(player2, new MerchantRaiders());
        castMerchantRaiders(target);

        harness.performUntapStep(player2);
        harness.performUntapStep(player2);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An already tapped creature can be locked")
    void alreadyTappedTargetIsLocked() {
        Permanent target = addCreatureReady(player2, new MerchantRaiders());
        target.tap();
        castMerchantRaiders(target);

        harness.performUntapStep(player2);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Pirate entering does not trigger Merchant Raiders")
    void opponentPirateDoesNotTrigger() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new MerchantRaiders());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new FathomFleetBoarder(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Losing control of Merchant Raiders ends its existing lock")
    void sourceControlChangeEndsLock() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castMerchantRaiders(target);
        Permanent merchant = findPermanent(player1, "Merchant Raiders");

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new CoerciveRecruiter(), "{4}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, merchant.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(merchant);

        harness.performUntapStep(player2);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Its own entry triggers even when it is not a Pirate")
    void selfEntryTriggersWithoutPirateSubtype() {
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");
        Permanent target = addCreatureReady(player2, new MerchantRaiders());

        castMerchantRaiders(target);

        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
    }
}
