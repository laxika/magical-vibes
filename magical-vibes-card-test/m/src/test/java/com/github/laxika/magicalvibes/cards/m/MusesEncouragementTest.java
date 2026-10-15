package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MusesEncouragement.class})
class MusesEncouragementTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a flying 3/3 blue and red Elemental, then surveils 2")
    void createsElementalThenSurveilsTwo() {
        GameData gd = harness.getGameData();
        Card topCard = new MusesEncouragement();
        Card secondCard = new MusesEncouragement();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new MusesEncouragement()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);

        Permanent token = findPermanent(player1, "Elemental");
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard, secondCard);
        assertThat(surveil.toGraveyard()).isTrue();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard, secondCard);
    }

    @Test
    void canKeepBothCardsInEitherOrder() {
        Card first = new MusesEncouragement();
        Card second = new MusesEncouragement();
        Card third = new MusesEncouragement();
        resolveWithLibrary(List.of(first, second, third));

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second, third);
    }

    @Test
    void canPutOneCardInGraveyardAndKeepTheOther() {
        Card first = new MusesEncouragement();
        Card second = new MusesEncouragement();
        Card third = new MusesEncouragement();
        resolveWithLibrary(List.of(first, second, third));

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first).doesNotContain(second, third);
    }

    @Test
    void surveilsTheOnlyCardInAShortLibrary() {
        Card onlyCard = new MusesEncouragement();
        resolveWithLibrary(List.of(onlyCard));

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil.cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(onlyCard);
        harness.assertOnBattlefield(player1, "Elemental");
    }

    @Test
    void createsTokenEvenWithAnEmptyLibrary() {
        resolveWithLibrary(List.of());

        harness.assertOnBattlefield(player1, "Elemental");
        harness.assertInGraveyard(player1, "Muse's Encouragement");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void resolveWithLibrary(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new MusesEncouragement()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player1, 0);
    }
}
