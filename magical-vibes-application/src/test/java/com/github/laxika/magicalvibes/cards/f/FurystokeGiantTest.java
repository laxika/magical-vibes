package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FurystokeGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control gain a tap-to-deal-2-damage ability on ETB")
    void otherCreaturesGainDamageAbility() {
        harness.setLife(player2, 20);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // index 0

        castFurystokeGiant();

        // Bears (index 0) can now tap to deal 2 damage to any target.
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Furystoke Giant itself does not gain the granted ability")
    void giantDoesNotGainAbility() {
        addCreatureReady(player1, new GrizzlyBears()); // index 0

        castFurystokeGiant(); // giant enters at index 1

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Granted ability wears off at end of turn")
    void abilityWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new GrizzlyBears()); // index 0

        castFurystokeGiant();

        // End player1's turn — until-end-of-turn abilities are cleared during cleanup.
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to cleanup (resets "until end of turn" modifiers)

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    private void castFurystokeGiant() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FurystokeGiant()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the Giant → ETB trigger goes on the stack
        harness.passBothPriorities(); // resolve the ETB trigger → grant the ability
    }
}
