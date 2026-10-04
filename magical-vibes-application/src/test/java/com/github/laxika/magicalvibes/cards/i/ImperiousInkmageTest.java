package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImperiousInkmage.class})
class ImperiousInkmageTest extends BaseCardTest {

    private GameData resolveEtbSurveil() {
        GameData gd = harness.getGameData();
        harness.setHand(player1, List.of(new ImperiousInkmage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd;
    }

    private Card[] seedTopTwo(GameData gd) {
        Card top0 = new ImperiousInkmage();
        Card top1 = new ImperiousInkmage();
        List<Card> deck = new ArrayList<>(gd.playerDecks.get(player1.getId()));
        deck.addAll(0, List.of(top0, top1));
        harness.setLibrary(player1, deck);
        return new Card[]{top0, top1};
    }

    @Test
    @DisplayName("ETB creates a surveil 2 interaction")
    void etbEntersSurveilState() {
        GameData gd = resolveEtbSurveil();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).hasSize(2);
        assertThat(surveil.toGraveyard()).isTrue();
    }

    @Test
    @DisplayName("Surveil 2 can keep one card on top and put the other into the graveyard")
    void surveilSplitsTopAndGraveyard() {
        GameData gd = harness.getGameData();
        Card[] top = seedTopTwo(gd);

        resolveEtbSurveil();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()).get(0)).isSameAs(top[1]);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(top[0]);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(top[1]);
    }

    @Test
    void surveilCanKeepBothCardsInReverseOrder() {
        Card first = new ImperiousInkmage();
        Card second = new ImperiousInkmage();
        Card third = new ImperiousInkmage();
        harness.setLibrary(player1, List.of(first, second, third));

        resolveEtbSurveil();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void surveilCanPutBothCardsIntoGraveyard() {
        Card first = new ImperiousInkmage();
        Card second = new ImperiousInkmage();
        Card third = new ImperiousInkmage();
        harness.setLibrary(player1, List.of(first, second, third));

        resolveEtbSurveil();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void surveilWithOneCardCanPutItIntoGraveyard() {
        Card onlyCard = new ImperiousInkmage();
        harness.setLibrary(player1, List.of(onlyCard));

        resolveEtbSurveil();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(onlyCard);
    }

    @Test
    void surveilWithEmptyLibraryFinishesWithoutAChoice() {
        harness.setLibrary(player1, List.of());

        resolveEtbSurveil();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }
}
