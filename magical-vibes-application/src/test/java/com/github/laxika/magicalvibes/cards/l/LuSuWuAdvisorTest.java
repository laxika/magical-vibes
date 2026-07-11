package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LuSuWuAdvisorTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when the ability resolves")
    void drawsACard() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int before = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(before + 1);
    }

    @Test
    @DisplayName("Taps Lu Su when the ability is activated")
    void tapsOnActivation() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        assertThat(findPermanent(player1, "Lu Su, Wu Advisor").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can activate during the beginning of combat, before attackers are declared")
    void canActivateBeforeAttackers() {
        setupOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupOnMyTurn(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        harness.addToBattlefield(player1, new LuSuWuAdvisor());
        findPermanent(player1, "Lu Su, Wu Advisor").setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    private void setupOnMyTurn(TurnStep step) {
        harness.addToBattlefield(player1, new LuSuWuAdvisor());
        findPermanent(player1, "Lu Su, Wu Advisor").setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
