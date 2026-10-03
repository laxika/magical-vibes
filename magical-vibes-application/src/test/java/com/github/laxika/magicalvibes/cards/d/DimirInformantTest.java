package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DimirInformant.class})
class DimirInformantTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield surveils two")
    void entersWithSurveilTwo() {
        Card topCard = new DimirInformant();
        Card middleCard = new DimirInformant();
        harness.setLibrary(player1, List.of(topCard, middleCard));
        harness.castFromHand(player1, new DimirInformant(), "{2}{U}");
        resolveAllTriggers();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard, middleCard);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(middleCard);
    }

    @Test
    void canKeepBothCardsInReverseOrderWithoutDisturbingTheRestOfTheLibrary() {
        Card first = new DimirInformant();
        Card second = new DimirInformant();
        Card third = new DimirInformant();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.castFromHand(player1, new DimirInformant(), "{2}{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void canPutBothCardsIntoTheGraveyard() {
        Card first = new DimirInformant();
        Card second = new DimirInformant();
        Card third = new DimirInformant();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.castFromHand(player1, new DimirInformant(), "{2}{U}");
        resolveAllTriggers();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void surveilsTheOnlyCardWhenLibraryHasOneCard() {
        Card onlyCard = new DimirInformant();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.castFromHand(player1, new DimirInformant(), "{2}{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void resolvesWithoutAChoiceWhenLibraryIsEmpty() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new DimirInformant(), "{2}{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(countPermanents(player1, "Dimir Informant")).isEqualTo(1);
    }
}
