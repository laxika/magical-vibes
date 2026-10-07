package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ToucanPuffin.class, GrizzlyBears.class})
class ToucanPuffinTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives a creature you control +2/+0 until end of turn")
    void etbBoostsCreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ToucanPuffin()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = bears.getId();
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ToucanPuffin()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = bears.getId();
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ToucanPuffin()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @CardUsed({ToucanPuffin.class})
    @DisplayName("Can target itself when entering an otherwise empty battlefield")
    void canTargetItself() {
        harness.setHand(player1, List.of(new ToucanPuffin()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent puffin = findPermanent(player1, "Toucan-Puffin");
        harness.handlePermanentChosen(player1, puffin.getId());
        harness.passBothPriorities();

        assertThat(puffin.getEffectivePower()).isEqualTo(4);
        assertThat(puffin.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @CardUsed({ToucanPuffin.class})
    @DisplayName("Entry without being cast still triggers the boost")
    void enteringWithoutBeingCastTriggersBoost() {
        Permanent puffin = harness.enterBattlefieldAndReturn(player1, new ToucanPuffin());
        harness.handlePermanentChosen(player1, puffin.getId());
        harness.passBothPriorities();

        assertThat(puffin.getEffectivePower()).isEqualTo(4);
        assertThat(puffin.getEffectiveToughness()).isEqualTo(2);
    }
}
