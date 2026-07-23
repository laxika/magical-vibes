package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PlayerSourceNextDamageShield;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageFromSelfToYouEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PreventNextDamageFromSelfToYouEffect}: installs a one-shot CoP-style shield
 * protecting the activating player from the next damage event dealt by the ability's source.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PreventNextDamageFromSelfToYouEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventNextDamageFromSelfToYouEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getSourcePermanentId();
        UUID playerId = entry.getControllerId();
        if (sourceId == null || playerId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null) {
            return;
        }

        gameData.playerSourceNextDamageShields.add(new PlayerSourceNextDamageShield(playerId, sourceId));

        String playerName = gameData.playerIdToName.get(playerId);
        String sourceName = source.getCard().getName();
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                "The next time " + sourceName + " would deal damage to " + playerName
                        + " this turn, it is prevented."));
        log.info("Game {} - {} shielded from next damage by {}", gameData.id, playerName, sourceName);
    }
}
