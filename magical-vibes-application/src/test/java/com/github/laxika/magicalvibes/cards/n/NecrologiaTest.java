package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NecrologiaTest extends BaseCardTest {

    private void prepareCaster() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Necrologia()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
    }

    @Test
    @DisplayName("Cast during your end step, pay X life and draw X cards")
    void payXLifeDrawXCards() {
        prepareCaster();
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0);
        harness.passBothPriorities(); // resolve -> prompts for X

        harness.handleXValueChosen(player1, 3);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing X=0 pays no life and draws nothing")
    void chooseZeroDoesNothing() {
        prepareCaster();
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("X choice is capped at the caster's current life total")
    void xChoiceCappedAtLife() {
        prepareCaster();
        int life = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        var choice = (com.github.laxika.magicalvibes.model.PendingInteraction.XValueChoice)
                gd.interaction.activeInteraction();
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxValue()).isEqualTo(life);
    }

    @Test
    @DisplayName("Cannot cast outside your end step")
    void cannotCastOutsideEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Necrologia()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
