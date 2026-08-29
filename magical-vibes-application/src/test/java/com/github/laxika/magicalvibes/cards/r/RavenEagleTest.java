package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavenEagle.class, GrizzlyBears.class, Shock.class})
class RavenEagleTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles up to one card and creates a Clue for a creature card")
    void etbExilesCreatureAndCreatesClue() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        harness.enterBattlefieldAndReturn(player1, new RavenEagle());

        chooseGraveyardCard(bears);

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(bears.getId()));
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("ETB exiles a noncreature card without creating a Clue")
    void etbExilesNoncreatureWithoutClue() {
        Card shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));

        harness.enterBattlefieldAndReturn(player1, new RavenEagle());

        chooseGraveyardCard(shock);

        harness.assertNotInGraveyard(player2, "Shock");
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("ETB may choose no graveyard card")
    void etbMayChooseNoCard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        harness.enterBattlefieldAndReturn(player1, new RavenEagle());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Attacking exiles up to one card from any graveyard")
    void attackExilesCreatureFromGraveyardAndCreatesClue() {
        addReadyRaven();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        declareAttackers(List.of(0));

        chooseGraveyardCard(bears);

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Attacking with empty graveyards still puts the optional trigger on the stack")
    void attackWithEmptyGraveyardsStillTriggers() {
        addReadyRaven();

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Draining triggers when its controller draws their second card each turn")
    void drainsOnControllerSecondCardDraw() {
        harness.addToBattlefieldAndReturn(player1, new RavenEagle());
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock()));
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

    private Permanent addReadyRaven() {
        Permanent raven = new Permanent(new RavenEagle());
        raven.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(raven);
        return raven;
    }

    private void chooseGraveyardCard(Card card) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
    }

    private void drawAndResolveTrigger(com.github.laxika.magicalvibes.model.Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
