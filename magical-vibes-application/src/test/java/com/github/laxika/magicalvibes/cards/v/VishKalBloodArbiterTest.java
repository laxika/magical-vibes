package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VishKalBloodArbiter.class, GrizzlyBears.class})
class VishKalBloodArbiterTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature puts counters on Vish Kal equal to its effective power")
    void sacrificeCreatureAddsCountersEqualToPower() {
        Permanent vishKal = addCreatureReady(player1, new VishKalBloodArbiter());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(vishKal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Removing counters gives a target creature a matching temporary debuff")
    void removesCountersAndDebuffsTargetCreature() {
        Permanent vishKal = addCreatureReady(player1, new VishKalBloodArbiter());
        vishKal.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bears.getId());

        assertThat(vishKal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, bears)).isZero();
    }

    @Test
    @DisplayName("The second ability cannot target a noncreature")
    void secondAbilityCannotTargetNoncreature() {
        Permanent vishKal = addCreatureReady(player1, new VishKalBloodArbiter());
        vishKal.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(vishKal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
