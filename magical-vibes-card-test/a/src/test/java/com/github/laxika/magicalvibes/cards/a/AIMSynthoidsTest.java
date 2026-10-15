package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AIMSynthoids.class})
class AIMSynthoidsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a surveil 2 ability")
    void entersWithSurveilTwo() {
        Card topCard = new AIMSynthoids();
        Card secondCard = new AIMSynthoids();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new AIMSynthoids()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard, secondCard);
        assertThat(surveil.toGraveyard()).isTrue();
    }

    @Test
    @DisplayName("Surveil 2 can put both cards into the graveyard")
    void surveilTwoPutsCardsIntoGraveyard() {
        Card topCard = new AIMSynthoids();
        Card secondCard = new AIMSynthoids();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new AIMSynthoids()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard, secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard, secondCard);
    }

    @Test
    @DisplayName("Surveil can keep both cards in a different order without disturbing the rest")
    void keepsAndReordersBothCards() {
        Card topCard = new AIMSynthoids();
        Card secondCard = new AIMSynthoids();
        Card thirdCard = new AIMSynthoids();
        harness.setLibrary(player1, List.of(topCard, secondCard, thirdCard));
        harness.setHand(player1, List.of(new AIMSynthoids()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, topCard, thirdCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Surveil can put one card into the graveyard and keep the other on top")
    void splitsCardsBetweenLibraryAndGraveyard() {
        Card topCard = new AIMSynthoids();
        Card secondCard = new AIMSynthoids();
        Card thirdCard = new AIMSynthoids();
        harness.setLibrary(player1, List.of(topCard, secondCard, thirdCard));
        harness.setHand(player1, List.of(new AIMSynthoids()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, thirdCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Surveil 2 with a one-card library looks at only that card")
    void surveilsOnlyAvailableCard() {
        Card onlyCard = new AIMSynthoids();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new AIMSynthoids()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Surveil 2 with an empty library finishes without a choice")
    void emptyLibraryFinishesWithoutChoice() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new AIMSynthoids()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "A.I.M. Synthoids");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
