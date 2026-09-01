package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NorthPolePatrol.class, GrizzlyBears.class, Plains.class})
class NorthPolePatrolTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps another permanent you control")
    void untapsAnotherPermanentYouControl() {
        Permanent patrol = addReadyPatrol();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        land.tap();

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(patrol.isTapped()).isTrue();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Waterbend taps an opposing creature and the permanents used to pay")
    void waterbendTapsOpposingCreature() {
        Permanent patrol = addReadyPatrol();
        Permanent firstPayment = addReadyCreature(player1);
        Permanent secondPayment = addReadyCreature(player1);
        Permanent thirdPayment = addReadyCreature(player1);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(patrol.isTapped()).isTrue();
        assertThat(firstPayment.isTapped()).isTrue();
        assertThat(secondPayment.isTapped()).isTrue();
        assertThat(thirdPayment.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Waterbend cannot target a creature you control")
    void waterbendCannotTargetYourCreature() {
        addReadyPatrol();
        addReadyCreature(player1);
        addReadyCreature(player1);
        addReadyCreature(player1);
        Permanent ownCreature = addReadyCreature(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    private Permanent addReadyPatrol() {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new NorthPolePatrol());
        patrol.setSummoningSick(false);
        return patrol;
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }
}
