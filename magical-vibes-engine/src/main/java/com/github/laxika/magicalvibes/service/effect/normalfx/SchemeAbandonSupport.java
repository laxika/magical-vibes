package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Abandons schemes (CR 701.33b): the scheme is turned face down and put on the bottom of its
 * owner's scheme deck. The scheme is either face up in the command zone, or a permanent where
 * the engine still models an ongoing scheme on the battlefield.
 */
@Component
@RequiredArgsConstructor
public class SchemeAbandonSupport {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;

    /** Whether the scheme is still face up in the command zone, or still on the battlefield. */
    public boolean isSchemeInPlay(GameData gameData, Card scheme, UUID sourcePermanentId) {
        return gameQueryService.findPermanentById(gameData, sourcePermanentId) != null
                || gameData.isFaceUpScheme(scheme);
    }

    /** Abandons the scheme, returning false when it is no longer face up or on the battlefield. */
    public boolean abandon(GameData gameData, Card scheme, UUID sourcePermanentId) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        Card abandoned = scheme;
        if (permanent != null) {
            abandoned = permanent.getCard();
            if (!permanentRemovalService.removePermanentToCommandZone(gameData, permanent)) {
                return false;
            }
        } else if (!gameData.isFaceUpScheme(scheme)) {
            return false;
        }
        if (!gameData.abandonScheme(abandoned)) {
            return false;
        }
        gameLogService.append(gameData, GameLog.cardThen(abandoned, " is abandoned."));
        return true;
    }
}
