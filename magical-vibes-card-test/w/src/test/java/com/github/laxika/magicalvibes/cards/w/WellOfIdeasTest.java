package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WellOfIdeas.class, SongOfTheDryads.class})
class WellOfIdeasTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }

    @Test
    @DisplayName("Entering the battlefield draws two cards")
    void enteringTheBattlefieldDrawsTwoCards() {
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.enterBattlefieldAndReturn(player1, new WellOfIdeas());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 2);
    }

    @Test
    @DisplayName("Controller draws two additional cards during their draw step")
    void controllerDrawsTwoAdditionalCards() {
        harness.addToBattlefield(player1, new WellOfIdeas());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 3);
    }

    @Test
    @DisplayName("Each opponent draws one additional card during their draw step")
    void opponentDrawsOneAdditionalCard() {
        harness.addToBattlefield(player1, new WellOfIdeas());
        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        advanceToDraw(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 2);
    }

    @Test
    @DisplayName("The normal draw occurs before the additional draw trigger resolves")
    void additionalDrawUsesTheStack() {
        harness.addToBattlefield(player1, new WellOfIdeas());
        int handBefore = gd.playerHands.get(player2.getId()).size();

        advanceToDraw(player2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("A draw trigger still resolves after Well of Ideas leaves the battlefield")
    void drawTriggerSurvivesItsSource() {
        Permanent well = harness.addToBattlefieldAndReturn(player1, new WellOfIdeas());
        int handBefore = gd.playerHands.get(player2.getId()).size();
        advanceToDraw(player2);
        gd.playerBattlefields.get(player1.getId()).remove(well);
        gd.playerGraveyards.get(player1.getId()).add(well.getCard());

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Multiple Wells each add two cards to their controller's draw step")
    void multipleWellsAddControllerDraws() {
        harness.addToBattlefield(player1, new WellOfIdeas());
        harness.addToBattlefield(player1, new WellOfIdeas());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToDraw(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 5);
    }

    @Test
    @DisplayName("Wells controlled by different players give the active player three additional cards")
    void opposingWellsDrawForTheActivePlayer() {
        harness.addToBattlefield(player1, new WellOfIdeas());
        harness.addToBattlefield(player2, new WellOfIdeas());
        int activeHandBefore = gd.playerHands.get(player2.getId()).size();
        int otherHandBefore = gd.playerHands.get(player1.getId()).size();

        advanceToDraw(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(activeHandBefore + 4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(otherHandBefore);
    }

    @Test
    @DisplayName("Well of Ideas turned into a Forest does not trigger on an opponent's draw step")
    void abilityLossPreventsOpponentDrawTrigger() {
        Permanent well = harness.addToBattlefieldAndReturn(player1, new WellOfIdeas());
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, well.getId());
        resolveAllTriggers();
        int handBefore = gd.playerHands.get(player2.getId()).size();

        advanceToDraw(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
    }
}
