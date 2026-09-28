package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesCardsFromHandThenMayCastOneEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the two-stage hidden hand choice used by Extract Brain. */
@Component
@RequiredArgsConstructor
public class TargetPlayerChoosesCardsFromHandThenMayCastOneEffectHandler
        implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final CardRevealService cardRevealService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerChoosesCardsFromHandThenMayCastOneEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choiceEffect = (TargetPlayerChoosesCardsFromHandThenMayCastOneEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.getOrDefault(targetPlayerId, List.of());
        int count = amountEvaluationService.evaluate(
                gameData, choiceEffect.count(), AmountContext.forStackEntry(entry, null));
        int cardsToChoose = Math.min(Math.max(count, 0), hand.size());
        if (cardsToChoose == 0) {
            return;
        }

        List<Integer> validIndices = allIndices(hand.size());
        PendingInteraction.TargetPlayerChoosesCardsFromHandChoice choice =
                new PendingInteraction.TargetPlayerChoosesCardsFromHandChoice(
                        targetPlayerId,
                        targetPlayerId,
                        entry.getControllerId(),
                        validIndices,
                        cardsToChoose,
                        List.of(),
                        entry.getCard().getName() + " — choose " + cardsToChoose + " card"
                                + (cardsToChoose == 1 ? "" : "s") + " from your hand.");

        if (cardsToChoose == hand.size()) {
            gameData.interaction.clearAwaitingInput();
            queueCastOffers(gameData, entry.getControllerId(), targetPlayerId, hand);
            return;
        }

        interactionHandlerRegistry.begin(gameData, choice);
    }

    /** Applies one answer in the target player's multi-pick hand selection. */
    public boolean handleCardSelection(GameData gameData,
                                       PendingInteraction.TargetPlayerChoosesCardsFromHandChoice choice,
                                       int cardIndex) {
        if (!choice.validIndices().contains(cardIndex)) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        List<Card> hand = gameData.playerHands.getOrDefault(choice.targetPlayerId(), List.of());
        if (cardIndex < 0 || cardIndex >= hand.size()) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        List<UUID> selectedCardIds = new ArrayList<>(choice.selectedCardIds());
        UUID selectedCardId = hand.get(cardIndex).getId();
        if (!selectedCardIds.contains(selectedCardId)) {
            selectedCardIds.add(selectedCardId);
        }

        int remainingCount = choice.remainingCount() - 1;
        List<Integer> nextValidIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (!selectedCardIds.contains(hand.get(i).getId())) {
                nextValidIndices.add(i);
            }
        }

        if (remainingCount > 0 && !nextValidIndices.isEmpty()) {
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.TargetPlayerChoosesCardsFromHandChoice(
                            choice.choosingPlayerId(), choice.targetPlayerId(), choice.controllerId(),
                            nextValidIndices, remainingCount, selectedCardIds, choice.prompt()));
            return false;
        }

        gameData.interaction.clearAwaitingInput();
        List<Card> selectedCards = hand.stream()
                .filter(card -> selectedCardIds.contains(card.getId()))
                .toList();
        queueCastOffers(gameData, choice.controllerId(), choice.targetPlayerId(), selectedCards);
        return true;
    }

    private void queueCastOffers(GameData gameData, UUID controllerId, UUID targetPlayerId,
                                 List<Card> selectedCards) {
        cardRevealService.revealToPlayer(
                gameData, targetPlayerId,
                com.github.laxika.magicalvibes.model.event.GameEventFact.RevealZone.HAND,
                selectedCards, controllerId);

        for (int i = selectedCards.size() - 1; i >= 0; i--) {
            Card card = selectedCards.get(i);
            if (card.hasType(CardType.LAND)) {
                continue;
            }
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    controllerId,
                    List.of(new MayCastFromHandWithoutPayingManaCostEffect(false)),
                    "Cast " + card.getName() + " without paying its mana cost?"));
        }
    }

    private List<Integer> allIndices(int size) {
        List<Integer> indices = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            indices.add(i);
        }
        return indices;
    }
}
