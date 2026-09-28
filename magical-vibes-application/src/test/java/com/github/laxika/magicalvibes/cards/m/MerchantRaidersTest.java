package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FathomFleetBoarder;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerchantRaiders.class, FathomFleetBoarder.class, GrizzlyBears.class})
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

        harness.setHand(player1, List.of(new FathomFleetBoarder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
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

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getUntapPreventedWhileSourceOnBattlefieldIds()).isEmpty();
    }

    @Test
    @DisplayName("The trigger may choose no target")
    void mayChooseNoTarget() {
        harness.setHand(player1, List.of(new MerchantRaiders()));
        addMerchantMana();

        harness.castCreature(player1, 0);
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
        advanceToNextTurn(player1);

        assertThat(target.isTapped()).isFalse();
    }

    private void castMerchantRaiders(Permanent target) {
        harness.setHand(player1, List.of(new MerchantRaiders()));
        addMerchantMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        chooseTargetAndResolve(target);
    }

    private void chooseTargetAndResolve(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void addMerchantMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
