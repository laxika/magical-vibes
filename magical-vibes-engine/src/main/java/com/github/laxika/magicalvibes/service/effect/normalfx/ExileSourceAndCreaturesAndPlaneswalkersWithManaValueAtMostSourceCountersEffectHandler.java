package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceAndCreaturesAndPlaneswalkersWithManaValueAtMostSourceCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExileSourceAndCreaturesAndPlaneswalkersWithManaValueAtMostSourceCountersEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSourceAndCreaturesAndPlaneswalkersWithManaValueAtMostSourceCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileSourceAndCreaturesAndPlaneswalkersWithManaValueAtMostSourceCountersEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent sourceForCounters = source != null ? source : entry.getSourcePermanentSnapshot();
        if (sourceForCounters == null) {
            return;
        }

        int maxManaValue = sourceForCounters.getCounterCount(e.counterType());
        List<Permanent> permanentsToExile = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (source != null && permanent.getId().equals(source.getId())) {
                    continue;
                }
                if ((gameQueryService.isCreature(gameData, permanent)
                        || gameQueryService.isPlaneswalker(gameData, permanent))
                        && permanent.getCard().getManaValue() <= maxManaValue) {
                    permanentsToExile.add(permanent);
                }
            }
        });

        List<Card> cardsToExile = new ArrayList<>();
        for (List<Card> graveyard : gameData.playerGraveyards.values()) {
            for (Card card : graveyard) {
                if ((card.hasType(CardType.CREATURE) || card.hasType(CardType.PLANESWALKER))
                        && card.getManaValue() <= maxManaValue) {
                    cardsToExile.add(card);
                }
            }
        }

        if (source != null && permanentRemovalService.removePermanentToExile(gameData, source)) {
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), " is exiled."));
            log.info("Game {} - {} exiles itself", gameData.id, source.getCard().getName());
        }
        for (Permanent permanent : permanentsToExile) {
            if (permanentRemovalService.removePermanentToExile(gameData, permanent)) {
                gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is exiled."));
                log.info("Game {} - {} is exiled by {}",
                        gameData.id, permanent.getCard().getName(), entry.getCard().getName());
            }
        }
        for (Card card : cardsToExile) {
            if (graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, card.getId(), card)) {
                gameLogService.append(gameData, GameLog.textCardText("Exiles ", card, " from a graveyard."));
                log.info("Game {} - {} is exiled from a graveyard by {}",
                        gameData.id, card.getName(), entry.getCard().getName());
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
