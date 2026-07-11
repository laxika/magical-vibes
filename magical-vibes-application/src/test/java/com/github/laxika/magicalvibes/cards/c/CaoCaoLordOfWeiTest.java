package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaoCaoLordOfWeiTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent discards two cards of their choice")
    void targetOpponentDiscardsTwoCards() {
        setupCaoCaoOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest(), new GiantGrowth())));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Taps Cao Cao when the ability is activated")
    void tapsOnActivation() {
        setupCaoCaoOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(findPermanent(player1, "Cao Cao, Lord of Wei").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupCaoCaoOnMyTurn(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        harness.addToBattlefield(player1, new CaoCaoLordOfWei());
        findPermanent(player1, "Cao Cao, Lord of Wei").setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Cannot target the ability's controller")
    void cannotTargetSelf() {
        setupCaoCaoOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setupCaoCaoOnMyTurn(TurnStep step) {
        harness.addToBattlefield(player1, new CaoCaoLordOfWei());
        findPermanent(player1, "Cao Cao, Lord of Wei").setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
