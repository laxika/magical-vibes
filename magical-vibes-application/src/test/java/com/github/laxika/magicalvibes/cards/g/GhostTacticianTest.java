package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostTactician.class, GrizzlyBears.class})
class GhostTacticianTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card boosts all creatures you control")
    void boostsOwnCreatures() {
        Permanent tactician = addCreatureReady(player1, new GhostTactician());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(tactician.getPowerModifier()).isEqualTo(1);
        assertThat(ownBears.getPowerModifier()).isEqualTo(1);
        assertThat(opponentBears.getPowerModifier()).isZero();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ability cannot be activated without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new GhostTactician());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card");
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent tactician = addCreatureReady(player1, new GhostTactician());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(tactician.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(tactician.getPowerModifier()).isZero();
    }
}
