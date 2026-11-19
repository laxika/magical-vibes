package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves a self-exile rider ({@link ExileTargetPermanentEffect} with {@code source()}) on a mana ability. This keeps
 * the source-removal effect in the ability's resolution order instead of incorrectly modeling it as an activation
 * cost. A mana ability has no target (CR 605.1a), so no other subject can reach it.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExileTargetPermanentManaAbilityEffectHandler implements ManaAbilityEffectHandler {

    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        CardEffect effect, int manaMultiplier, boolean creatureSource) {
        var exile = (ExileTargetPermanentEffect) effect;
        if (!(exile.subject() instanceof PermanentSubject.Source)) {
            throw new IllegalStateException("A mana ability can only exile its source: " + exile.subject());
        }
        if (permanent == null) {
            return;
        }
        if (exile.outsideGame()) {
            permanentRemovalService.removePermanentToOutsideGame(gameData, permanent);
            gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is removed from the game."));
        } else {
            if (!permanentRemovalService.removePermanentToExile(gameData, permanent)) {
                return;
            }
            gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is exiled."));
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
        log.info("Game {} - {} exiles itself", gameData.id, permanent.getCard().getName());
    }
}
