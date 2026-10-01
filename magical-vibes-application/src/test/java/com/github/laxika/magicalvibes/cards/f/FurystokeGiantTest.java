package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FurystokeGiant.class, GrizzlyBears.class})
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

    @Test
    @DisplayName("Persist returns the Giant with a -1/-1 counter and its ETB grants the ability again")
    void persistReturnsGiantAndTriggersEtbAgain() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GrizzlyBears()); // index 0
        addCreatureReady(player1, new GrizzlyBears()); // index 1
        addCreatureReady(player1, new GrizzlyBears()); // index 2

        castFurystokeGiant();
        Permanent giant = findPermanent(player1, "Furystoke Giant");

        harness.activateAbility(player1, 0, null, giant.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, giant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returnedGiant = findPermanent(player1, "Furystoke Giant");
        assertThat(returnedGiant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, returnedGiant)).isEqualTo(2);

        harness.activateAbility(player1, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private void castFurystokeGiant() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new FurystokeGiant(), "{3}{R}{R}");
        harness.passBothPriorities(); // resolve the Giant → ETB trigger goes on the stack
        harness.passBothPriorities(); // resolve the ETB trigger → grant the ability
    }
}
