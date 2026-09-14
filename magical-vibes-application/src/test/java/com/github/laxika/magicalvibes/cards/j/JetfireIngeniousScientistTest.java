package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MishrasBauble;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JetfireIngeniousScientist.class, JetfireAirGuardian.class,
        MishrasBauble.class, GrizzlyBears.class})
class JetfireIngeniousScientistTest extends BaseCardTest {

    @Test
    void moreThanMeetsTheEyeCastsJetfireConverted() {
        harness.setHand(player1, List.of(new JetfireIngeniousScientist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent jetfire = findPermanent(player1, "Jetfire, Air Guardian");
        assertThat(jetfire.isTransformed()).isTrue();
        assertThat(jetfire.getCard()).isInstanceOf(JetfireAirGuardian.class);
        assertThat(gqs.isCreature(gd, jetfire)).isTrue();
    }

    @Test
    void removesCountersFromArtifactAddsRestrictedManaToTargetAndConverts() {
        Permanent jetfire = harness.addToBattlefieldAndReturn(player1, new JetfireIngeniousScientist());
        Permanent bauble = harness.addToBattlefieldAndReturn(player1, new MishrasBauble());
        bauble.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(bauble.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(jetfire.isTransformed()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getPowerstoneOnlyColorless()).isEqualTo(2);
    }

    @Test
    void backFaceConvertsThenAdapts() {
        Permanent jetfire = castConvertedJetfire();
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, 0, 0, null);
        harness.passBothPriorities();

        assertThat(jetfire.isTransformed()).isFalse();
        assertThat(jetfire.getCard()).isInstanceOf(JetfireIngeniousScientist.class);
        assertThat(jetfire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void countersOnNonartifactsCannotPayTheAbility() {
        harness.addToBattlefield(player1, new JetfireIngeniousScientist());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent castConvertedJetfire() {
        harness.setHand(player1, List.of(new JetfireIngeniousScientist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Jetfire, Air Guardian");
    }
}
