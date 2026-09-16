package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CommonBond;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArceeSharpshooter.class, ArceeAcrobaticCoupe.class, CommonBond.class, HillGiant.class})
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
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(arcee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target1.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target2.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent castArceeConverted() {
        harness.setHand(player1, List.of(new ArceeSharpshooter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Arcee, Acrobatic Coupe");
    }
}
