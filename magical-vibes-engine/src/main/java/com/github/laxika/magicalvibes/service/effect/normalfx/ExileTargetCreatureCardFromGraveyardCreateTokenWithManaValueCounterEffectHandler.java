package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardFromGraveyardCreateTokenWithManaValueCounterEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileTargetCreatureCardFromGraveyardCreateTokenWithManaValueCounterEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PermanentControlSupport permanentControlSupport;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureCardFromGraveyardCreateTokenWithManaValueCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTargetCreatureCardFromGraveyardCreateTokenWithManaValueCounterEffect) effect;
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null && !entry.getTargetCardIds().isEmpty()) {
            targetCardId = entry.getTargetCardIds().getFirst();
        }
        Card targetCard = targetCardId == null
                ? null : gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null || !targetCard.hasType(CardType.CREATURE)) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target is no longer a creature card in a graveyard)."));
            return;
        }

        int counters = targetCard.getManaValue() >= e.minimumManaValue() ? e.counterAmount() : 0;
        if (!graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, targetCardId, targetCard)) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target no longer in a graveyard)."));
            return;
        }

        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(entry.getControllerId()) + " exiles ", targetCard,
                " from a graveyard."));

        List<UUID> createdIds = permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), e.tokenTemplate().withAmount(1), entry.getCard().getSetCode());
        entry.getCreatedPermanentIds().addAll(createdIds);
        if (counters <= 0) {
            return;
        }
        for (UUID createdId : createdIds) {
            Permanent createdToken = gameQueryService.findPermanentById(gameData, createdId);
            if (createdToken != null && !gameQueryService.cantHaveCounters(gameData, createdToken)) {
                permanentCounterSupport.placeCounterOnPermanent(
                        gameData, entry, createdToken, CounterType.PLUS_ONE_PLUS_ONE, counters);
            }
        }
    }
}
