package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowCastCardsExiledWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllowCastCardsExiledWithSourceUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowCastCardsExiledWithSourceUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AllowCastCardsExiledWithSourceUntilEndOfTurnEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) return;

        List<Card> matchingCards = gameData.getCardsExiledByPermanent(sourcePermanentId).stream()
                .filter(card -> e.filter() == null
                        || predicateEvaluationService.matchesCardPredicate(card, e.filter(), null))
                .toList();
        if (matchingCards.isEmpty()) return;

        UUID grantId = UUID.randomUUID();
        for (Card card : matchingCards) {
            gameData.exileCastPermissionsUntilEndOfTurn.add(new GameData.ExileCastPermission(
                    grantId, sourcePermanentId, entry.getControllerId(), card.getId(),
                    e.withoutPayingManaCost()));
        }

        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(entry.getControllerId())
                + " may cast a card exiled with " + entry.getCard().getName()
                + " until end of turn."));
        log.info("Game {} - {} may cast one of {} card(s) exiled with {} until end of turn",
                gameData.id, gameData.playerIdToName.get(entry.getControllerId()), matchingCards.size(),
                entry.getCard().getName());
    }
}
