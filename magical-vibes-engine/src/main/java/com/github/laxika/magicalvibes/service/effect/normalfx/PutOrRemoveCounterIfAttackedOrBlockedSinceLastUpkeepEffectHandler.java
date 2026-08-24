package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutOrRemoveCounterIfAttackedOrBlockedSinceLastUpkeepEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves the upkeep counter adjustment for permanents that attacked or blocked during the
 * window since the previous relevant upkeep.
 */
@Component
@RequiredArgsConstructor
public class PutOrRemoveCounterIfAttackedOrBlockedSinceLastUpkeepEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutOrRemoveCounterIfAttackedOrBlockedSinceLastUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutOrRemoveCounterIfAttackedOrBlockedSinceLastUpkeepEffect) effect;
        Permanent permanent = findReferencedPermanent(gameData, entry, e.reference());
        if (permanent == null) {
            return;
        }

        boolean attackedOrBlocked = permanent.isAttackedOrBlockedSinceLastUpkeep();
        permanent.setAttackedOrBlockedSinceLastUpkeep(false);

        String counterName = permanentCounterSupport.counterTypeName(e.counterType());
        if (attackedOrBlocked) {
            permanentCounterSupport.placeCounterOnPermanent(gameData, entry, permanent, e.counterType(), 1);
            return;
        }

        int current = permanent.getCounterCount(e.counterType());
        if (current <= 0) {
            return;
        }
        permanent.setCounterCount(e.counterType(), current - 1);
        gameLogService.append(gameData, GameLog.builder().card(permanent.getCard())
                .text(" removes a " + counterName + " counter.").build());
    }

    private Permanent findReferencedPermanent(GameData gameData, StackEntry entry,
                                              PermanentReference reference) {
        return switch (reference) {
            case SOURCE -> findPermanent(gameData, entry.getSourcePermanentId());
            case TRIGGERING -> findPermanent(gameData, entry.getTriggeringPermanentId());
            case ATTACHED -> {
                Permanent source = findPermanent(gameData, entry.getSourcePermanentId());
                yield source == null || !source.isAttached()
                        ? null
                        : findPermanent(gameData, source.getAttachedTo());
            }
            case RETURNED -> findPermanentByCardId(gameData, entry.getTargetId());
        };
    }

    private Permanent findPermanent(GameData gameData, UUID permanentId) {
        return permanentId == null ? null : gameQueryService.findPermanentById(gameData, permanentId);
    }

    private Permanent findPermanentByCardId(GameData gameData, UUID cardId) {
        if (cardId == null) {
            return null;
        }
        return gameData.playerBattlefields.values().stream()
                .filter(java.util.Objects::nonNull)
                .flatMap(java.util.Collection::stream)
                .filter(permanent -> cardId.equals(permanent.getCard().getId())
                        || (permanent.getOriginalCard() != null
                        && cardId.equals(permanent.getOriginalCard().getId())))
                .findFirst()
                .orElse(null);
    }
}
