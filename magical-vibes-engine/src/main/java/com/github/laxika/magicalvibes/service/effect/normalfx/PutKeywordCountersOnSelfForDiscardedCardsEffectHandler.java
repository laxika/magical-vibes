package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutKeywordCountersOnSelfForDiscardedCardsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutKeywordCountersOnSelfForDiscardedCardsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutKeywordCountersOnSelfForDiscardedCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            return;
        }

        EnumSet<CounterType> countersToAdd = EnumSet.noneOf(CounterType.class);
        List<UUID> discardedCardIds = entry.getTriggeringCardIds().isEmpty()
                ? entry.getTriggeringCardId() == null ? List.of() : List.of(entry.getTriggeringCardId())
                : entry.getTriggeringCardIds();
        List<Card> discardedCards = discardedCardIds.stream()
                .map(cardId -> gameQueryService.findCardInGraveyardById(gameData, cardId))
                .filter(card -> card != null)
                .toList();
        for (Card card : discardedCards) {
            for (CounterType counterType : ((PutKeywordCountersOnSelfForDiscardedCardsEffect) effect)
                    .counterTypes()) {
                if (card.getKeywords().contains(counterType.grantedKeyword())) {
                    countersToAdd.add(counterType);
                }
            }
        }

        for (CounterType counterType : countersToAdd) {
            permanentCounterSupport.placeCounterOnPermanent(gameData, entry, source, counterType, 1);
        }
    }
}
