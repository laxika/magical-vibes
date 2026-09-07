package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToCombatDamageCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileUpToCombatDamageCardsFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PermanentCounterSupport permanentCounterSupport;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileUpToCombatDamageCardsFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> exiledCards = new ArrayList<>();
        List<UUID> targetCardIds = entry.getTargetCardIds();
        if (targetCardIds != null) {
            for (UUID cardId : targetCardIds) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                if (card != null && graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, cardId, card)) {
                    exiledCards.add(card);
                }
            }
        }

        if (!exiledCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " exiles " + exiledCards.size()
                            + " card" + (exiledCards.size() == 1 ? "" : "s") + " from graveyard."));
        }

        int creatureCards = (int) exiledCards.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .count();
        if (creatureCards > 0 && entry.getSourcePermanentId() != null) {
            Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (source != null) {
                permanentCounterSupport.applyPlusOnePlusOneCounters(gameData, entry, source, creatureCards);
            }
        }

        int noncreatureCards = (int) exiledCards.stream()
                .filter(card -> !card.hasType(CardType.CREATURE))
                .count();
        if (noncreatureCards > 0) {
            lifeSupport.applyGainLife(gameData, controllerId, noncreatureCards,
                    entry.getCard().getName(), entry.getCard(), entry.getEntryType());
        }
    }
}
