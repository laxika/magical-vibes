package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CemeteryDesecratorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChosenCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Cemetery Desecrator's mandatory graveyard exile and reflexive modal ability. */
@Component
@RequiredArgsConstructor
public class CemeteryDesecratorEffectHandler implements NormalEffectHandlerBean {

    private static final String REMOVE_COUNTERS_MODE = "Remove X counters from target permanent";
    private static final String DEBUFF_MODE =
            "Target creature an opponent controls gets -X/-X until end of turn";

    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CemeteryDesecratorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var state = gameData.graveyardTargetOperation;
        if (state.resolutionTimeExileThenEffectChoiceMade) {
            UUID chosenCardId = state.resolutionTimeExileThenEffectChosenCardId;
            state.resolutionTimeExileThenEffectChoiceMade = false;
            state.resolutionTimeExileThenEffectChosenCardId = null;
            gameData.rerunCurrentEffectAfterInteraction = false;

            Card chosen = findMatchingCard(gameData, entry, chosenCardId);
            if (chosen != null) {
                exileAndQueueModal(gameData, entry, chosen);
            }
            return;
        }

        List<Card> candidates = matchingCards(gameData, entry);
        if (candidates.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), " finds no other card in a graveyard to exile."));
            return;
        }

        state.resolutionTimeExileThenEffectResume = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoice(gameData, entry.getControllerId(),
                new ArrayList<>(candidates), 1, 1,
                entry.getCard().getName() + " — Choose another card from a graveyard to exile.");
    }

    private List<Card> matchingCards(GameData gameData, StackEntry entry) {
        UUID sourceCardId = entry.getCard().getId();
        List<Card> cards = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) {
                continue;
            }
            for (Card card : graveyard) {
                if (!card.getId().equals(sourceCardId)) {
                    cards.add(card);
                }
            }
        }
        return cards;
    }

    private Card findMatchingCard(GameData gameData, StackEntry entry, UUID cardId) {
        if (cardId == null) {
            return null;
        }
        return matchingCards(gameData, entry).stream()
                .filter(card -> card.getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }

    private void exileAndQueueModal(GameData gameData, StackEntry entry, Card chosen) {
        if (!graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, chosen.getId(), chosen)) {
            return;
        }

        int manaValue = chosen.getManaValue();
        var opponentCreatureFilter = TargetFilters.creatureAnOpponentControls();
        ChooseOneEffect modal = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        REMOVE_COUNTERS_MODE,
                        new RemoveChosenCountersFromTargetPermanentEffect(manaValue),
                        TargetFilters.permanent()),
                new ChooseOneEffect.ChooseOneOption(
                        DEBUFF_MODE,
                        new BoostTargetCreatureEffect(-manaValue, -manaValue,
                                opponentCreatureFilter.predicate()),
                        opponentCreatureFilter)));

        gameLogService.append(gameData,
                GameLog.textCardText(entry.getCard().getName() + " exiles ", chosen,
                        " from a graveyard."));
        gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                entry.getCard(), entry.getControllerId(), modal, entry.getSourcePermanentId()));
    }
}
