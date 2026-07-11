package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShuFarmerTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when the ability resolves")
    void gainsLife() {
        setupFarmerOnMyTurn(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Taps the farmer when the ability is activated")
    void tapsOnActivation() {
        setupFarmerOnMyTurn(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        assertThat(findPermanent(player1, "Shu Farmer").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can activate during the beginning of combat, before attackers are declared")
    void canActivateBeforeAttackers() {
        setupFarmerOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupFarmerOnMyTurn(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        harness.addToBattlefield(player1, new ShuFarmer());
        findPermanent(player1, "Shu Farmer").setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    private void setupFarmerOnMyTurn(TurnStep step) {
        harness.addToBattlefield(player1, new ShuFarmer());
        findPermanent(player1, "Shu Farmer").setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
