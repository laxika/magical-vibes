package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.t.Thoughtcast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindsEye.class, Thoughtcast.class})
class MindsEyeTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }

    @Test
    @DisplayName("Opponent draw triggers a may-pay ability")
    void triggersOnOpponentDraw() {
        harness.addToBattlefield(player1, new MindsEye());

        advanceToDraw(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Paying {1} draws a card")
    void payingDrawsCard() {
        harness.addToBattlefield(player1, new MindsEye());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToDraw(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Declining does not draw a card")
    void decliningDoesNotDraw() {
        harness.addToBattlefield(player1, new MindsEye());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToDraw(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Having no mana does not draw a card")
    void cannotPayDoesNotDraw() {
        harness.addToBattlefield(player1, new MindsEye());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToDraw(player2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Controller draw does not trigger")
    void doesNotTriggerOnControllerDraw() {
        harness.addToBattlefield(player1, new MindsEye());

        advanceToDraw(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent drawing two cards triggers twice")
    void triggersForEachCardDrawn() {
        harness.addToBattlefield(player1, new MindsEye());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Thoughtcast()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }
}
