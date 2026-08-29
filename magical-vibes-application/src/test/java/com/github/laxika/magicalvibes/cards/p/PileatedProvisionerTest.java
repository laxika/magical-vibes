package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
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

@CardUsed({PileatedProvisioner.class, GrizzlyBears.class, WindDrake.class})
class PileatedProvisionerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on a target creature you control without flying")
    void etbPutsCounterOnTargetCreatureWithoutFlying() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PileatedProvisioner()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature with flying")
    void cannotTargetCreatureWithFlying() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WindDrake());
        harness.setHand(player1, List.of(new PileatedProvisioner()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control without flying");
    }

    @Test
    @DisplayName("Cannot target a creature controlled by an opponent")
    void cannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PileatedProvisioner()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control without flying");
    }

    @Test
    @DisplayName("ETB does not trigger when there is no legal target")
    void etbDoesNotTriggerWithoutLegalTarget() {
        harness.addToBattlefield(player1, new WindDrake());
        harness.setHand(player1, List.of(new PileatedProvisioner()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
