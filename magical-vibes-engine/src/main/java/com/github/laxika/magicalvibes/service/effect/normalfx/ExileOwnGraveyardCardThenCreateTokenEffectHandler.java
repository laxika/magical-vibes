package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnGraveyardCardThenCreateTokenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves an optional own-graveyard exile and its immediate token-creation rider. */
@Component
@RequiredArgsConstructor
public class ExileOwnGraveyardCardThenCreateTokenEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileOwnGraveyardCardThenCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileThenCreate = (ExileOwnGraveyardCardThenCreateTokenEffect) effect;
        var state = gameData.graveyardTargetOperation;

        if (state.resolutionTimeExileThenEffectChoiceMade) {
            UUID chosenCardId = state.resolutionTimeExileThenEffectChosenCardId;
            state.resolutionTimeExileThenEffectChoiceMade = false;
            state.resolutionTimeExileThenEffectChosenCardId = null;
            gameData.rerunCurrentEffectAfterInteraction = false;

            if (chosenCardId == null) {
                return;
            }

            Card chosen = findMatchingCard(gameData, entry, exileThenCreate, chosenCardId);
            if (chosen != null) {
                exileAndCreateToken(gameData, entry, exileThenCreate, chosen);
            }
            return;
        }

        List<Card> candidates = matchingCards(gameData, entry, exileThenCreate);
        if (candidates.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), " finds no matching card in its controller's graveyard to exile."));
            return;
        }

        state.resolutionTimeExileThenEffectResume = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoice(gameData, entry.getControllerId(), candidates, 1, 0,
                entry.getCard().getName() + " — You may exile a matching card from your graveyard.");
    }

    private List<Card> matchingCards(GameData gameData, StackEntry entry,
                                     ExileOwnGraveyardCardThenCreateTokenEffect effect) {
        List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
        if (graveyard == null) {
            return List.of();
        }
        UUID sourceCardId = entry.getCard().getId();
        return graveyard.stream()
                .filter(card -> !card.getId().equals(sourceCardId))
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, effect.filter(), sourceCardId, gameData, entry.getControllerId()))
                .toList();
    }

    private Card findMatchingCard(GameData gameData, StackEntry entry,
                                  ExileOwnGraveyardCardThenCreateTokenEffect effect, UUID cardId) {
        return matchingCards(gameData, entry, effect).stream()
                .filter(card -> card.getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }

    private void exileAndCreateToken(GameData gameData, StackEntry entry,
                                     ExileOwnGraveyardCardThenCreateTokenEffect effect, Card card) {
        UUID controllerId = entry.getControllerId();
        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, card.getId());
        exileService.exileCard(gameData, controllerId, card);
        gameLogService.append(gameData, GameLog.textCardText(
                entry.getCard().getName() + " exiles ", card, " from its controller's graveyard."));

        CreateTokenEffect token = effect.tokenTemplate().withAmount(1);
        entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                gameData, controllerId, token, 1, entry.getCard().getSetCode(),
                token.tokenPower(), token.tokenToughness()));
    }
}
