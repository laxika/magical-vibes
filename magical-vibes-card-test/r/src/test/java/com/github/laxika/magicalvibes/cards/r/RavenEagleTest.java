package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavenEagle.class, Island.class})
class RavenEagleTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles up to one card and creates a Clue for a creature card")
    void etbExilesCreatureAndCreatesClue() {
        Card creature = new RavenEagle();
        harness.setGraveyard(player2, List.of(creature));

        harness.enterBattlefieldAndReturn(player1, new RavenEagle());

        chooseGraveyardCard(creature);

        harness.assertNotInGraveyard(player2, "Raven Eagle");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("ETB exiles a noncreature card without creating a Clue")
    void etbExilesNoncreatureWithoutClue() {
        Card noncreature = new Island();
        harness.setGraveyard(player2, List.of(noncreature));

        harness.enterBattlefieldAndReturn(player1, new RavenEagle());

        chooseGraveyardCard(noncreature);

        harness.assertNotInGraveyard(player2, "Island");
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("ETB may choose no graveyard card")
    void etbMayChooseNoCard() {
        Card creature = new RavenEagle();
        harness.setGraveyard(player2, List.of(creature));

        harness.enterBattlefieldAndReturn(player1, new RavenEagle());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Raven Eagle");
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Attacking exiles up to one card from any graveyard")
    void attackExilesCreatureFromGraveyardAndCreatesClue() {
        addCreatureReady(player1, new RavenEagle());
        Card creature = new RavenEagle();
        harness.setGraveyard(player2, List.of(creature));

        declareAttackers(List.of(0));

        chooseGraveyardCard(creature);

        harness.assertNotInGraveyard(player2, "Raven Eagle");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Attacking with empty graveyards still puts the optional trigger on the stack")
    void attackWithEmptyGraveyardsStillTriggers() {
        addCreatureReady(player1, new RavenEagle());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Draining triggers when its controller draws their second card each turn")
    void drainsOnControllerSecondCardDraw() {
        harness.addToBattlefieldAndReturn(player1, new RavenEagle());
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        int player1Life = gd.playerLifeTotals.get(player1.getId());
        int player2Life = gd.playerLifeTotals.get(player2.getId());

        drawAndResolveTrigger(player1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1Life);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(player2Life);

        drawAndResolveTrigger(player1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1Life + 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(player2Life - 1);

        drawAndResolveTrigger(player1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1Life + 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(player2Life - 1);
    }

    @Test
    void canExileCreatureFromOwnGraveyard() {
        Card creature = new RavenEagle();
        harness.setGraveyard(player1, List.of(creature));
        harness.enterBattlefieldAndReturn(player1, new RavenEagle());

        chooseGraveyardCard(creature);

        harness.assertNotInGraveyard(player1, "Raven Eagle");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.playersWhoInvestigatedThisTurn).doesNotContain(player1.getId());
    }

    @Test
    void targetLeavingGraveyardBeforeResolutionDoesNotCreateClue() {
        Card creature = new RavenEagle();
        harness.setGraveyard(player2, List.of(creature));
        harness.enterBattlefieldAndReturn(player1, new RavenEagle());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(creature));

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void opponentsSecondDrawDoesNotDrain() {
        harness.addToBattlefieldAndReturn(player1, new RavenEagle());
        harness.setLibrary(player2, List.of(new Island(), new Island()));

        drawAndResolveTrigger(player2);
        drawAndResolveTrigger(player2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void ravenEnteringAfterSecondDrawDoesNotTriggerOnThirdDraw() {
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        drawAndResolveTrigger(player1);
        drawAndResolveTrigger(player1);
        harness.addToBattlefieldAndReturn(player1, new RavenEagle());

        drawAndResolveTrigger(player1);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificingCreatedClueDrawsSecondCardAndDrains() {
        Card creature = new RavenEagle();
        harness.setGraveyard(player2, List.of(creature));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new RavenEagle());
        chooseGraveyardCard(creature);
        drawAndResolveTrigger(player1);
        Permanent clue = findPermanents(player1, "Clue").getFirst();
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, clueIndex, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    private void chooseGraveyardCard(Card card) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
    }

    private void drawAndResolveTrigger(com.github.laxika.magicalvibes.model.Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        resolveAllTriggers();
    }
}
