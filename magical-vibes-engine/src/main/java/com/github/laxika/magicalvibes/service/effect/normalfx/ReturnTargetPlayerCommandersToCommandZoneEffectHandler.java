package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPlayerCommandersToCommandZoneEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnTargetPlayerCommandersToCommandZoneEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetPlayerCommandersToCommandZoneEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        List<Permanent> commanders = gameData.playerBattlefields
                .getOrDefault(targetPlayerId, List.of())
                .stream()
                .filter(permanent -> isCommander(gameData, permanent))
                .toList();

        for (Permanent commander : commanders) {
            if (permanentRemovalService.removePermanentToCommandZone(gameData, commander)) {
                gameLogService.append(gameData, GameLog.cardThen(
                        commander.getCard(), " is returned to its owner's command zone."));
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private boolean isCommander(GameData gameData, Permanent permanent) {
        Card originalCard = permanent.getOriginalCard();
        return permanent.isCommander() || originalCard != null && gameData.isCommander(originalCard.getId());
    }
}
