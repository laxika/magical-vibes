package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AnchovyBananaPizza;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
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

    private void castRaptor() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RagamuffinRaptor()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
