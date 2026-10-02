package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CommonBond;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SunderingGrowth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArceeSharpshooter.class, ArceeAcrobaticCoupe.class, CommonBond.class, HillGiant.class,
        SunderingGrowth.class})
class ArceeSharpshooterTest extends BaseCardTest {

    @Test
    void moreThanMeetsTheEyeCastsArceeConvertedAndLivingMetalApplies() {
        Permanent arcee = castArceeConverted();

        assertThat(arcee.isTransformed()).isTrue();
        assertThat(arcee.getCard()).isInstanceOf(ArceeAcrobaticCoupe.class);
        assertThat(gqs.isCreature(gd, arcee)).isTrue();
    }

    @Test
    void removesCountersDealsThatMuchDamageAndConverts() {
        Permanent arcee = harness.addToBattlefieldAndReturn(player1, new ArceeSharpshooter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        arcee.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, 2, target.getId());
        harness.passBothPriorities();

        assertThat(arcee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(arcee.isTransformed()).isTrue();
        assertThat(arcee.getCard()).isInstanceOf(ArceeAcrobaticCoupe.class);
    }

    @Test
    void putsOneCounterForEachControlledCreatureTarget() {
        Permanent arcee = castArceeConverted();
        Permanent target1 = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target2 = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new CommonBond()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, List.of(target1.getId(), target2.getId()));
        resolveAllTriggers();

        assertThat(arcee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target1.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target2.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void targetingArceeAddsCountersAndConvertsBeforeTheSpellResolves() {
        Permanent arcee = castArceeConverted();
        harness.setHand(player1, List.of(new CommonBond()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, List.of(arcee.getId()));
        harness.passBothPriorities();

        assertThat(arcee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(arcee.isTransformed()).isFalse();
        assertThat(arcee.getCard()).isInstanceOf(ArceeSharpshooter.class);

        resolveAllTriggers();
        assertThat(arcee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void livingMetalDoesNotMakeArceeACreatureDuringOpponentsTurn() {
        Permanent arcee = castArceeConverted();

        harness.forceActivePlayer(player2);
        assertThat(gqs.isCreature(gd, arcee)).isFalse();

        harness.forceActivePlayer(player1);
        assertThat(gqs.isCreature(gd, arcee)).isTrue();
    }

    @Test
    void normalManaCostCastsFrontFace() {
        harness.castFromHand(player1, new ArceeSharpshooter(), "{1}{R}{W}");
        resolveAllTriggers();

        Permanent arcee = findPermanent(player1, "Arcee, Sharpshooter");
        assertThat(arcee.isTransformed()).isFalse();
    }

    @Test
    void onlyControlledTargetsContributeCounters() {
        Permanent arcee = castArceeConverted();
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new CommonBond()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, List.of(ownTarget.getId(), opposingTarget.getId()));
        resolveAllTriggers();

        assertThat(arcee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void spellTargetingOnlyOpposingCreaturesDoesNotTrigger() {
        Permanent arcee = castArceeConverted();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArceeSharpshooter());
        harness.setHand(player1, List.of(new CommonBond()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, List.of(target.getId()));
        resolveAllTriggers();

        assertThat(arcee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(arcee.isTransformed()).isTrue();
    }

    @Test
    void canRemoveOnlySomeCountersAndPaysThemBeforeResolution() {
        Permanent arcee = harness.addToBattlefieldAndReturn(player1, new ArceeSharpshooter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArceeSharpshooter());
        arcee.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        arcee.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, 1, target.getId());

        assertThat(arcee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(arcee.isTransformed()).isFalse();
        assertThat(target.getMarkedDamage()).isZero();

        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(arcee.isTransformed()).isTrue();
        assertThat(arcee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotRemoveZeroOrMoreCountersThanAvailable() {
        Permanent arcee = harness.addToBattlefieldAndReturn(player1, new ArceeSharpshooter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArceeSharpshooter());
        arcee.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(arcee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void stackedActivationsDealDamageButConvertOnlyOnce() {
        Permanent arcee = harness.addToBattlefieldAndReturn(player1, new ArceeSharpshooter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        arcee.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, 1, target.getId());
        harness.ensurePriority(player1);
        harness.activateAbility(player1, 0, 0, 1, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(arcee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(arcee.isTransformed()).isTrue();
        assertThat(arcee.getCard()).isInstanceOf(ArceeAcrobaticCoupe.class);
    }

    @Test
    void targetingNoncreatureVehicleDuringOpponentsTurnTriggersAndConverts() {
        Permanent arcee = castArceeConverted();
        harness.forceActivePlayer(player2);
        assertThat(gqs.isCreature(gd, arcee)).isFalse();
        harness.setHand(player1, List.of(new SunderingGrowth()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.ensurePriority(player1);

        harness.castInstant(player1, 0, arcee.getId());
        harness.passBothPriorities();

        assertThat(arcee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(arcee.isTransformed()).isFalse();
        assertThat(gqs.isCreature(gd, arcee)).isTrue();

        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Arcee, Sharpshooter");
    }

    @Test
    void opponentsSpellTargetingControlledCreatureDoesNotTrigger() {
        Permanent arcee = castArceeConverted();
        harness.setHand(player2, List.of(new CommonBond()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);

        harness.castInstant(player2, 0, List.of(arcee.getId()));
        resolveAllTriggers();

        assertThat(arcee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(arcee.isTransformed()).isTrue();
    }

    @Test
    void nontargetedSpellDoesNotTrigger() {
        Permanent arcee = castArceeConverted();

        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        resolveAllTriggers();

        assertThat(arcee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(arcee.isTransformed()).isTrue();
    }

    @Test
    void illegalTargetOnResolutionPreventsDamageAndConversionButDoesNotRefundCosts() {
        Permanent arcee = harness.addToBattlefieldAndReturn(player1, new ArceeSharpshooter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArceeSharpshooter());
        arcee.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, 1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        resolveAllTriggers();

        assertThat(arcee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(arcee.isTransformed()).isFalse();
        assertThat(target.getMarkedDamage()).isZero();
    }

    private Permanent castArceeConverted() {
        harness.setHand(player1, List.of(new ArceeSharpshooter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();
        return findPermanent(player1, "Arcee, Acrobatic Coupe");
    }
}
