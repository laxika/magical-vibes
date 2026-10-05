package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LostLegion.class})
class LostLegionTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldOffersScryTwo() {
        castLostLegion();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);
    }

    @Test
    void scryTwoCanReorderTheTopOfTheLibrary() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card topCard = deck.get(0);
        Card secondCard = deck.get(1);

        castLostLegion();
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(deck).containsSubsequence(secondCard, topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void scryCanKeepOneCardAndBottomTheOther() {
        Card first = new LostLegion();
        Card second = new LostLegion();
        Card third = new LostLegion();
        harness.setLibrary(player1, List.of(first, second, third));
        List<Card> opponentLibrary = List.copyOf(gd.playerDecks.get(player2.getId()));

        castLostLegion();
        resolveAllTriggers();
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.playerId()).isEqualTo(player1.getId());
        assertThat(scry.cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentLibrary);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void scryCanBottomBothCardsInEitherOrder() {
        Card first = new LostLegion();
        Card second = new LostLegion();
        Card third = new LostLegion();
        harness.setLibrary(player1, List.of(first, second, third));

        castLostLegion();
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, second, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void scryWithOneCardLooksAtOnlyThatCard() {
        Card onlyCard = new LostLegion();
        harness.setLibrary(player1, List.of(onlyCard));

        castLostLegion();
        resolveAllTriggers();
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void scryWithAnEmptyLibraryFinishesWithoutAChoice() {
        harness.setLibrary(player1, List.of());

        castLostLegion();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Lost Legion");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
    private void castLostLegion() {
        harness.setHand(player1, List.of(new LostLegion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }

}
