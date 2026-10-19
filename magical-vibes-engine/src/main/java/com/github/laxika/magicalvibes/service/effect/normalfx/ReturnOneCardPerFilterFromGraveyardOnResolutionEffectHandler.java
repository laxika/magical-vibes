package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnOneCardPerFilterFromGraveyardOnResolutionEffect;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Resolves a per-filter resolution-time graveyard choice: asks for up to one card per filter in
 * order, then returns all picked cards to the battlefield together.
 */
@Component
@RequiredArgsConstructor
public class ReturnOneCardPerFilterFromGraveyardOnResolutionEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnOneCardPerFilterFromGraveyardOnResolutionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var returnEffect = (ReturnOneCardPerFilterFromGraveyardOnResolutionEffect) effect;
        GraveyardTargetOperationState state = gameData.graveyardTargetOperation;
        UUID controllerId = entry.getControllerId();

        int index;
        if (state.resolutionTimeOneCardPerFilterChoiceMade) {
            UUID chosenCardId = state.resolutionTimeOneCardPerFilterChosenCardId;
            state.resolutionTimeOneCardPerFilterChoiceMade = false;
            state.resolutionTimeOneCardPerFilterChosenCardId = null;
            gameData.rerunCurrentEffectAfterInteraction = false;
            if (chosenCardId != null && !state.resolutionTimeOneCardPerFilterChosenIds.contains(chosenCardId)) {
                state.resolutionTimeOneCardPerFilterChosenIds.add(chosenCardId);
            }
            index = state.resolutionTimeOneCardPerFilterIndex + 1;
        } else {
            state.resolutionTimeOneCardPerFilterChosenIds = new ArrayList<>();
            index = 0;
        }

        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(controllerId, List.of());
        for (; index < returnEffect.filters().size(); index++) {
            int filterIndex = index;
            List<Card> candidates = graveyard.stream()
                    .filter(card -> predicateEvaluationService.matchesCardPredicate(
                            card, returnEffect.filters().get(filterIndex), entry.getCard().getId()))
                    .toList();
            if (candidates.isEmpty()) {
                continue;
            }
            state.resolutionTimeOneCardPerFilterIndex = index;
            state.resolutionTimeOneCardPerFilterResume = true;
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginMultiGraveyardChoice(gameData, controllerId, candidates, 1, 0,
                    entry.getCard().getName() + " \u2014 You may return a "
                            + returnEffect.descriptions().get(index) + " from your graveyard to the battlefield.");
            return;
        }

        List<UUID> chosenIds = state.resolutionTimeOneCardPerFilterChosenIds;
        state.resolutionTimeOneCardPerFilterChosenIds = new ArrayList<>();
        List<Card> cards = chosenIds.stream()
                .map(id -> graveyard.stream().filter(card -> card.getId().equals(id)).findFirst().orElse(null))
                .filter(card -> card != null)
                .toList();
        for (Card card : cards) {
            permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
        }
        if (!cards.isEmpty()) {
            graveyardReturnSupport.putCardsOntoBattlefieldSimultaneously(
                    gameData, Map.of(controllerId, cards), false, null);
        }
        int returned = (int) cards.stream()
                .filter(card -> gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                        .map(Permanent::getCard)
                        .anyMatch(onBattlefield -> onBattlefield.getId().equals(card.getId())))
                .count();
        entry.setEventValue(returned);
    }
}
