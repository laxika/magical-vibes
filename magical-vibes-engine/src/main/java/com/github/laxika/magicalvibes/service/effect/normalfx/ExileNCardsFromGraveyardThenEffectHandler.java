package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardThenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/** Resolves an exact graveyard exile and its reflexive follow-up trigger. */
@Component
@RequiredArgsConstructor
public class ExileNCardsFromGraveyardThenEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final ExileService exileService;
    private final GraveyardService graveyardService;
    private final TriggeredAbilityQueueService triggeredAbilityQueueService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileNCardsFromGraveyardThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileThen = (ExileNCardsFromGraveyardThenEffect) effect;
        var state = gameData.graveyardTargetOperation;
        UUID controllerId = entry.getControllerId();

        List<UUID> chosenCardIds = state.resolutionTimeExileNCardsThenEffectChosenCardIds;
        if (chosenCardIds != null) {
            state.resolutionTimeExileNCardsThenEffectResume = false;
            state.resolutionTimeExileNCardsThenEffectChosenCardIds = null;
            gameData.rerunCurrentEffectAfterInteraction = false;

            if (chosenCardIds.size() != exileThen.count()
                    || new HashSet<>(chosenCardIds).size() != exileThen.count()) {
                return;
            }

            List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
            List<Card> chosenCards = graveyard == null ? List.of() : chosenCardIds.stream()
                    .map(cardId -> graveyard.stream()
                            .filter(card -> card.getId().equals(cardId))
                            .findFirst()
                            .orElse(null))
                    .filter(card -> card != null && predicateEvaluationService.matchesCardPredicate(
                            card, exileThen.filter(), entry.getCard() == null ? null : entry.getCard().getId()))
                    .toList();
            if (chosenCards.size() != exileThen.count()) {
                return;
            }

            exileAndQueueFollowUp(gameData, entry, exileThen, chosenCards);
            return;
        }

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        List<Card> candidates = graveyard == null ? List.of() : graveyard.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(card, exileThen.filter(), sourceCardId))
                .toList();
        if (candidates.size() < exileThen.count()) {
            return;
        }

        if (candidates.size() == exileThen.count()) {
            exileAndQueueFollowUp(gameData, entry, exileThen, candidates);
            return;
        }

        state.resolutionTimeExileNCardsThenEffectResume = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoice(
                gameData, controllerId, new ArrayList<>(candidates), exileThen.count(), exileThen.count(),
                entry.getCard().getName() + " — Choose exactly " + exileThen.count()
                        + " cards to exile from your graveyard.");
    }

    private void exileAndQueueFollowUp(GameData gameData, StackEntry entry,
                                        ExileNCardsFromGraveyardThenEffect effect,
                                        List<Card> cards) {
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        graveyard.removeAll(cards);
        graveyardService.notifyCardsExiledFromGraveyard(gameData, controllerId, cards);
        for (Card card : cards) {
            exileService.exileCard(gameData, controllerId, card);
        }

        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId) + " exiles " + cards.size()
                        + " cards from their graveyard."));

        if (effect.thenEffect().targetSpec().equals(TargetSpec.NONE)) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    entry.getCard(),
                    controllerId,
                    entry.getCard().getName() + "'s ability",
                    List.of(effect.thenEffect()),
                    null,
                    entry.getSourcePermanentId()));
        } else {
            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    entry.getCard(), controllerId, List.of(effect.thenEffect())));
            triggeredAbilityQueueService.processNextSpellGraveyardTargetTrigger(gameData);
        }
    }
}
