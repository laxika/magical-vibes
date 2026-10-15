package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AnchovyBananaPizza;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RagamuffinRaptor.class, AnchovyBananaPizza.class, GrizzlyBears.class, Shock.class})
class RagamuffinRaptorTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureToHand() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        castRaptor();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void returnsTargetFoodToHand() {
        Card target = new AnchovyBananaPizza();
        harness.setGraveyard(player1, List.of(target));

        castRaptor();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Anchovy & Banana Pizza");
        harness.assertNotInGraveyard(player1, "Anchovy & Banana Pizza");
    }

    @Test
    void filtersOutCardsThatAreNeitherCreatureNorFood() {
        harness.setGraveyard(player1, List.of(new Shock()));

        castRaptor();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void mayDeclineReturningAValidTarget() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        castRaptor();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    void offersOnlyOwnCreatureAndFoodCardsAndReturnsOnlyTheChosenCard() {
        Card creature = new RagamuffinRaptor();
        Card food = new AnchovyBananaPizza();
        Card nonMatching = new Shock();
        Card opposingCreature = new RagamuffinRaptor();
        harness.setGraveyard(player1, List.of(creature, food, nonMatching));
        harness.setGraveyard(player2, List.of(opposingCreature));

        castRaptor();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature.getId(), food.getId());
        harness.handleMultipleCardsChosen(player1, List.of(food.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Anchovy & Banana Pizza");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, nonMatching);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature);
    }

    @Test
    void entersWithAnEmptyGraveyardWithoutRequestingATarget() {
        harness.setGraveyard(player1, List.of());

        castRaptor();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Ragamuffin Raptor");
        harness.assertNotInHand(player1, "Ragamuffin Raptor");
    }

    @Test
    void doesNotChooseAnotherCardWhenTheTargetLeavesTheGraveyard() {
        Card target = new RagamuffinRaptor();
        Card other = new AnchovyBananaPizza();
        harness.setGraveyard(player1, List.of(target, other));

        castRaptor();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of(other));
        harness.setLibrary(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertNotInHand(player1, "Ragamuffin Raptor");
        harness.assertNotInHand(player1, "Anchovy & Banana Pizza");
        harness.assertInGraveyard(player1, "Anchovy & Banana Pizza");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target);
    }

    private void castRaptor() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new RagamuffinRaptor(), "{4}{G}");
        harness.passBothPriorities();
    }
}
