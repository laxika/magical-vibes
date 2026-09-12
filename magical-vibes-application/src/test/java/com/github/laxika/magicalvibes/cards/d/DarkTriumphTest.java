package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FlintGolem;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkTriumph.class, FlintGolem.class, Swamp.class})
class DarkTriumphTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +2/+0 until end of turn")
    void boostsOwnCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new FlintGolem());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new FlintGolem());

        castForMana();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(4);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new FlintGolem());

        castForMana();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can be cast for its alternate cost by sacrificing a creature while controlling a Swamp")
    void castsForAlternateCost() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent paymentCreature = harness.addToBattlefieldAndReturn(player1, new FlintGolem());
        Permanent survivingCreature = harness.addToBattlefieldAndReturn(player1, new FlintGolem());
        harness.setHand(player1, List.of(new DarkTriumph()));

        harness.castWithAlternateCost(player1, 0, List.of(paymentCreature.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Flint Golem");
        assertThat(survivingCreature.getEffectivePower()).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Alternate cost requires control of a Swamp")
    void alternateCostRequiresSwamp() {
        Permanent paymentCreature = harness.addToBattlefieldAndReturn(player1, new FlintGolem());
        harness.setHand(player1, List.of(new DarkTriumph()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(
                player1, 0, List.of(paymentCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
    }

    @Test
    @DisplayName("Alternate cost requires a creature to sacrifice")
    void alternateCostRequiresCreature() {
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new DarkTriumph()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castForMana() {
        harness.castFromHand(player1, new DarkTriumph(), "{4}{B}");
        harness.passBothPriorities();
    }
}
