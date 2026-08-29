package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOneOfDiscardedCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileOneOfDiscardedCardsFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileOneOfDiscardedCardsFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<UUID> triggeringCardIds = entry.getTriggeringCardIds();
        List<UUID> candidateIds = triggeringCardIds.isEmpty() && entry.getTriggeringCardId() != null
                ? List.of(entry.getTriggeringCardId()) : triggeringCardIds;
        List<Card> candidates = gameData.playerGraveyards.getOrDefault(controllerId, List.of()).stream()
                .filter(card -> candidateIds.contains(card.getId()))
                .filter(card -> !card.hasType(CardType.LAND))
                .toList();
        if (candidates.isEmpty()) {
            return;
        }

        var state = gameData.graveyardTargetOperation;
        if (state.resolutionTimeExileOneOfDiscardedCardsChoiceMade) {
            UUID chosenCardId = state.resolutionTimeExileOneOfDiscardedCardsChosenCardId;
            state.resolutionTimeExileOneOfDiscardedCardsChoiceMade = false;
            state.resolutionTimeExileOneOfDiscardedCardsChosenCardId = null;
            state.resolutionTimeExileOneOfDiscardedCardsCandidateIds = List.of();
            state.resolutionTimeExileOneOfDiscardedCardsSourcePermanentId = null;
            gameData.rerunCurrentEffectAfterInteraction = false;

            Card chosen = candidates.stream()
                    .filter(card -> card.getId().equals(chosenCardId))
                    .findFirst()
                    .orElse(null);
            if (chosen != null) {
                exile(gameData, entry, chosen, sourcePermanentId);
            }
            return;
        }

        if (candidates.size() == 1) {
            exile(gameData, entry, candidates.getFirst(), sourcePermanentId);
            return;
        }

        state.resolutionTimeExileOneOfDiscardedCardsResume = true;
        state.resolutionTimeExileOneOfDiscardedCardsCandidateIds = candidates.stream()
                .map(Card::getId)
                .toList();
        state.resolutionTimeExileOneOfDiscardedCardsSourcePermanentId = sourcePermanentId;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, candidates, 1, 1,
                "Choose one of the discarded nonland cards to exile.");
    }

    private void exile(GameData gameData, StackEntry entry, Card card, UUID sourcePermanentId) {
        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, card.getId());
        gameData.addToExile(entry.getControllerId(), card, sourcePermanentId);
        gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " exiles ", card,
                " from its controller's graveyard."));
    }
}
