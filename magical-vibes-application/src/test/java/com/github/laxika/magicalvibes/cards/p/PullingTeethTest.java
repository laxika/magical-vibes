package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.MothdustChangeling;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.cards.i.IntiSeneschalOfTheSun;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PullingTeeth.class, MothdustChangeling.class, Mutavault.class, IntiSeneschalOfTheSun.class})
class PullingTeethTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new PullingTeeth()));
        harness.addMana(player1, ManaColor.BLACK, 2); // {1}{B}
        harness.setHand(player2, List.of(
                new MothdustChangeling(), new MothdustChangeling(), new MothdustChangeling()));
    }

    private void keepRevealedCardsOnTop() {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).playerId())
                .isEqualTo(player1.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).playerId())
                .isEqualTo(player2.getId());
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("Winning the clash makes the target player discard two cards")
    void winningDiscardsTwo() {
        prepare();
        harness.setLibrary(player1, List.of(new MothdustChangeling(), new Mutavault()));
        harness.setLibrary(player2, List.of(new Mutavault(), new Mutavault()));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        keepRevealedCardsOnTop();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Losing the clash makes the target player discard only one card")
    void losingDiscardsOne() {
        prepare();
        harness.setLibrary(player1, List.of(new Mutavault(), new Mutavault()));
        harness.setLibrary(player2, List.of(new Mutavault(), new Mutavault()));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        keepRevealedCardsOnTop();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Winning the clash makes the caster discard when the caster is the target player")
    void winningDiscardsFromTheChosenTargetPlayer() {
        harness.setHand(player1, List.of(
                new PullingTeeth(), new MothdustChangeling(), new MothdustChangeling()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new MothdustChangeling(), new Mutavault()));
        harness.setLibrary(player2, List.of(new Mutavault(), new Mutavault()));

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        keepRevealedCardsOnTop();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Target player with an empty hand discards nothing even on a won clash")
    void emptyHandDiscardsNothing() {
        harness.setHand(player1, List.of(new PullingTeeth()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new MothdustChangeling(), new Mutavault()));
        harness.setLibrary(player2, List.of(new Mutavault(), new Mutavault()));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        keepRevealedCardsOnTop();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A strictly higher opposing reveal makes the target discard one card")
    void lowerManaValueDiscardsOne() {
        prepare();
        harness.setLibrary(player1, List.of(new Mutavault()));
        harness.setLibrary(player2, List.of(new MothdustChangeling()));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        keepRevealedCardsOnTop();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Winning with only one card in the target's hand discards that card")
    void winningWithOneCardInHand() {
        prepare();
        MothdustChangeling discarded = new MothdustChangeling();
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player1, List.of(new MothdustChangeling()));
        harness.setLibrary(player2, List.of(new Mutavault()));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        keepRevealedCardsOnTop();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("The won clash discards two cards as one discard event")
    void winningProducesOneDiscardEvent() {
        prepare();
        harness.addToBattlefield(player2, new IntiSeneschalOfTheSun());
        MothdustChangeling revealed = new MothdustChangeling();
        Mutavault second = new Mutavault();
        harness.setLibrary(player1, List.of(new PullingTeeth()));
        harness.setLibrary(player2, List.of(revealed, second));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        keepRevealedCardsOnTop();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(revealed);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("Clash placements wait for both players' decisions")
    void revealedCardsMoveTogetherAfterBothChoices() {
        prepare();
        MothdustChangeling casterTop = new MothdustChangeling();
        Mutavault casterBottom = new Mutavault();
        Mutavault opponentTop = new Mutavault();
        MothdustChangeling opponentBottom = new MothdustChangeling();
        harness.setLibrary(player1, List.of(casterTop, casterBottom));
        harness.setLibrary(player2, List.of(opponentTop, opponentBottom));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(casterTop, casterBottom);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop, opponentBottom);

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(casterBottom, casterTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentBottom, opponentTop);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }
}
