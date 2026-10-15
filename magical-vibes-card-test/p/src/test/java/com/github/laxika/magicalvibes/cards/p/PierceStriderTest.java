package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DivineOffering;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PierceStrider.class, DivineOffering.class})
class PierceStriderTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack with selected opponent target")
    void resolvingPutsEtbOnStackWithTarget() {
        castPierceStrider();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        harness.assertOnBattlefield(player1, "Pierce Strider");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB trigger makes target opponent lose 3 life")
    void etbMakesTargetOpponentLoseLife() {
        castPierceStrider();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB life loss works with non-default life totals")
    void etbLifeLossWithCustomTotals() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 5);

        castPierceStrider();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        castPierceStrider();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Game log records life loss")
    void gameLogRecordsLifeLoss() {
        castPierceStrider();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gameLogContains("loses 3 life")).isTrue();
    }

    @Test
    @DisplayName("Creature spell can be cast without choosing its triggered ability's target")
    void canCastWithoutChoosingTriggerTarget() {
        castPierceStrider();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isNull();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Trigger still causes life loss after its source is destroyed")
    void triggerResolvesAfterSourceLeaves() {
        castPierceStrider();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        harness.setHand(player2, List.of(new DivineOffering()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Pierce Strider"));

        harness.assertInGraveyard(player1, "Pierce Strider");
        harness.assertLife(player2, 24);
        resolveAllTriggers();
        harness.assertLife(player2, 21);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Opponent is relative to the trigger's controller")
    void otherControllerTargetsTheirOpponent() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new PierceStrider(), "{4}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    private void castPierceStrider() {
        harness.castFromHand(player1, new PierceStrider(), "{4}");
    }

    @Test
    @DisplayName("The enters trigger cannot target its controller")
    void triggerCannotTargetController() {
        castPierceStrider();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Entering without being cast also triggers the life loss")
    void enteringWithoutCastTriggersLifeLoss() {
        harness.enterBattlefieldAndReturn(player1, new PierceStrider());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Pierce Strider");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }
}
