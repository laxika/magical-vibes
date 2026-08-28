package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.ai.AiGameActions;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.ExileTargetNonlandPermanentAndCardWithSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiGraveyardChoiceAiStrategyTest {

    private final MultiGraveyardChoiceAiStrategy strategy = new MultiGraveyardChoiceAiStrategy();

    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private AiGameActions gameActions;

    private GameData gameData;
    private UUID aiPlayerId;

    @BeforeEach
    void setUp() {
        aiPlayerId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "test", aiPlayerId, "AI");
        gameData.graveyardTargetOperation.effects = new ArrayList<>(
                List.of(new ExileTargetNonlandPermanentAndCardWithSourceEffect()));
    }

    @Test
    @DisplayName("Mixed-zone choice skips battlefield cards beyond the zone maximum")
    void mixedZoneChoiceSkipsExtraBattlefieldCards() throws Exception {
        Card firstBattlefieldCard = new Card();
        Card secondBattlefieldCard = new Card();
        Card graveyardCard = new Card();
        when(gameQueryService.findCardInGraveyardById(any(), any()))
                .thenAnswer(invocation -> graveyardCard.getId().equals(invocation.getArgument(1))
                        ? graveyardCard : null);

        strategy.answer(choice(List.of(firstBattlefieldCard, secondBattlefieldCard, graveyardCard)), context());

        assertChosen(firstBattlefieldCard.getId(), graveyardCard.getId());
    }

    @Test
    @DisplayName("Mixed-zone choice skips graveyard cards beyond the zone maximum")
    void mixedZoneChoiceSkipsExtraGraveyardCards() throws Exception {
        Card firstGraveyardCard = new Card();
        Card secondGraveyardCard = new Card();
        Card battlefieldCard = new Card();
        when(gameQueryService.findCardInGraveyardById(any(), any()))
                .thenAnswer(invocation -> {
                    UUID cardId = invocation.getArgument(1);
                    if (firstGraveyardCard.getId().equals(cardId)) {
                        return firstGraveyardCard;
                    }
                    if (secondGraveyardCard.getId().equals(cardId)) {
                        return secondGraveyardCard;
                    }
                    return null;
                });

        strategy.answer(choice(List.of(firstGraveyardCard, secondGraveyardCard, battlefieldCard)), context());

        assertChosen(firstGraveyardCard.getId(), battlefieldCard.getId());
    }

    @Test
    @DisplayName("Choice does not exceed its maximum total mana value")
    void choiceDoesNotExceedMaximumTotalManaValue() throws Exception {
        Card sixManaCard = cardWithManaValue(6);
        Card threeManaCard = cardWithManaValue(3);
        gameData.graveyardTargetOperation.effects = new ArrayList<>();

        strategy.answer(choiceWithMaximumManaValue(List.of(sixManaCard, threeManaCard), 6), context());

        assertChosen(sixManaCard.getId());
    }

    @Test
    @DisplayName("Choice considers later cards that fit its maximum total mana value")
    void choiceConsidersLaterCardsWithinMaximumTotalManaValue() throws Exception {
        Card fourManaCard = cardWithManaValue(4);
        Card threeManaCard = cardWithManaValue(3);
        Card twoManaCard = cardWithManaValue(2);
        gameData.graveyardTargetOperation.effects = new ArrayList<>();

        strategy.answer(
                choiceWithMaximumManaValue(List.of(fourManaCard, threeManaCard, twoManaCard), 6),
                context());

        assertChosen(fourManaCard.getId(), twoManaCard.getId());
    }

    @Test
    @DisplayName("Per-graveyard choice takes the required cards without exceeding each limit")
    void perGraveyardChoiceTakesRequiredCardsWithinEachLimit() throws Exception {
        Card ownCard = new Card();
        List<Card> opponentCards = List.of(new Card(), new Card(), new Card(), new Card(), new Card());
        List<Card> cards = new ArrayList<>();
        cards.add(ownCard);
        cards.addAll(opponentCards);
        UUID opponentId = UUID.randomUUID();
        when(gameQueryService.findGraveyardOwnerById(any(), any()))
                .thenAnswer(invocation -> ownCard.getId().equals(invocation.getArgument(1))
                        ? aiPlayerId : opponentId);
        gameData.graveyardTargetOperation.effects = new ArrayList<>();
        gameData.graveyardTargetOperation
                .resolutionTimeShuffleUpToThreeCardsFromEachGraveyardResume = true;

        strategy.answer(new PendingInteraction.MultiGraveyardChoice(
                aiPlayerId, cards, 6, "Choose three cards in each graveyard.", 4), context());

        assertChosen(
                ownCard.getId(),
                opponentCards.get(0).getId(),
                opponentCards.get(1).getId(),
                opponentCards.get(2).getId());
    }

    private PendingInteraction.MultiGraveyardChoice choice(List<Card> cards) {
        return new PendingInteraction.MultiGraveyardChoice(
                aiPlayerId, cards, 2, "Choose up to two cards.");
    }

    private PendingInteraction.MultiGraveyardChoice choiceWithMaximumManaValue(
            List<Card> cards,
            int maximumTotalManaValue) {
        return new PendingInteraction.MultiGraveyardChoice(
                aiPlayerId, cards, 2, "Choose up to two cards.", 0, maximumTotalManaValue);
    }

    private Card cardWithManaValue(int manaValue) {
        Card card = mock(Card.class);
        when(card.getId()).thenReturn(UUID.randomUUID());
        when(card.getManaValue()).thenReturn(manaValue);
        return card;
    }

    private AiInteractionContext context() {
        return new AiInteractionContext(
                gameData, gameData.id, aiPlayerId, gameQueryService, gameActions);
    }

    private void assertChosen(UUID... expectedIds) throws Exception {
        ArgumentCaptor<InteractionAnswer> captor = ArgumentCaptor.forClass(InteractionAnswer.class);
        verify(gameActions).answerInteraction(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(InteractionAnswer.CardsChosen.class);
        assertThat(((InteractionAnswer.CardsChosen) captor.getValue()).cardIds())
                .containsExactly(expectedIds);
    }
}
