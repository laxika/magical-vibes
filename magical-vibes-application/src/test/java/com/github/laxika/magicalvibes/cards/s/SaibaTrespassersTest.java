package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaibaTrespassers.class, GrizzlyBears.class})
class SaibaTrespassersTest extends BaseCardTest {

    @Test
    void channelsToTapUpToTwoOpponentsCreaturesAndSkipTheirNextUntap() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SaibaTrespassers()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateHandAbilityWithMultiTargets(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(first.getSkipUntapCount()).isEqualTo(1);
        assertThat(second.isTapped()).isTrue();
        assertThat(second.getSkipUntapCount()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Saiba Trespassers");
    }

    @Test
    void channelCanChooseOnlyOneCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SaibaTrespassers()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateHandAbilityWithMultiTargets(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    void channelCannotTargetYourOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SaibaTrespassers()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateHandAbilityWithMultiTargets(
                player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Saiba Trespassers");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    void channelCanChooseZeroTargetsAndStillPaysItsCosts() {
        Permanent untouched = harness.addToBattlefieldAndReturn(player2, new SaibaTrespassers());
        prepareChannel();

        harness.activateHandAbilityWithMultiTargets(player1, 0, List.of());

        harness.assertInGraveyard(player1, "Saiba Trespassers");
        harness.assertNotInHand(player1, "Saiba Trespassers");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        assertThat(untouched.isTapped()).isFalse();
        assertThat(untouched.getSkipUntapCount()).isZero();
    }

    @Test
    void alreadyTappedCreatureSkipsOnlyItsControllersNextUntap() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SaibaTrespassers());
        target.setTapped(true);
        prepareChannel();

        harness.activateHandAbilityWithMultiTargets(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isZero();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void channelCannotChooseTheSameCreatureTwice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SaibaTrespassers());
        prepareChannel();

        assertThatThrownBy(() -> harness.activateHandAbilityWithMultiTargets(
                player1, 0, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Saiba Trespassers");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void channelCannotChooseThreeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SaibaTrespassers());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SaibaTrespassers());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new SaibaTrespassers());
        prepareChannel();

        assertThatThrownBy(() -> harness.activateHandAbilityWithMultiTargets(
                player1, 0, List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Saiba Trespassers");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    void channelStillAffectsRemainingTargetWhenAnotherLeavesBattlefield() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SaibaTrespassers());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SaibaTrespassers());
        prepareChannel();
        harness.activateHandAbilityWithMultiTargets(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(first);

        harness.passBothPriorities();

        assertThat(second.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(second.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    void channelRequiresBlueManaAndDoesNotDiscardWhenCostCannotBePaid() {
        harness.setHand(player1, List.of(new SaibaTrespassers()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateHandAbilityWithMultiTargets(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Saiba Trespassers");
        harness.assertNotInGraveyard(player1, "Saiba Trespassers");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    private void prepareChannel() {
        harness.setHand(player1, List.of(new SaibaTrespassers()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
