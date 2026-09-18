package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HogMonkeyRampage.class, AirElemental.class, GrizzlyBears.class, HillGiant.class})
class HogMonkeyRampageTest extends BaseCardTest {

    @Test
    @DisplayName("A creature with power 4 or greater gets a counter before fighting")
    void putsCounterBeforeFightWhenPowerIsAtLeastFour() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(attacker, blocker);

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(attacker.getEffectivePower()).isEqualTo(5);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature with power less than 4 still fights without getting a counter")
    void fightsWithoutCounterWhenPowerIsLessThanFour() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(attacker, blocker);

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Targets must be one creature you control and one creature an opponent controls")
    void rejectsInvalidTargetControllers() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HogMonkeyRampage()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(opponentCreature.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent ownCreature, Permanent opponentCreature) {
        harness.setHand(player1, List.of(new HogMonkeyRampage()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, List.of(ownCreature.getId(), opponentCreature.getId()));
        harness.passBothPriorities();
    }
}
