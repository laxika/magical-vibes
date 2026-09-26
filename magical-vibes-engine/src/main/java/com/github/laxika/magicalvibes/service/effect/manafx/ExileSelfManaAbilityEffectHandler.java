package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves a self-exile rider on a mana ability. This keeps the source-removal effect in the
 * ability's resolution order instead of incorrectly modeling it as an activation cost.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExileSelfManaAbilityEffectHandler implements ManaAbilityEffectHandler {

    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        CardEffect effect, int manaMultiplier, boolean creatureSource) {
        if (permanent == null || !permanentRemovalService.removePermanentToExile(gameData, permanent)) {
            return;
        }

        gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is exiled."));
        permanentRemovalService.removeOrphanedAuras(gameData);
        log.info("Game {} - {} exiles itself", gameData.id, permanent.getCard().getName());
    }
}
