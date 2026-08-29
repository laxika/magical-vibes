package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndAllControlledTokensWithSameNameEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPermanentAndAllControlledTokensWithSameNameEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentAndAllControlledTokensWithSameNameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        String targetName = target.getCard().getName();
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        List<Permanent> toExile = new ArrayList<>();
        toExile.add(target);

        if (targetControllerId != null) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(targetControllerId);
            if (battlefield != null) {
                for (Permanent permanent : battlefield) {
                    if (!permanent.getId().equals(target.getId())
                            && permanent.getCard().isToken()
                            && targetName.equals(permanent.getCard().getName())) {
                        toExile.add(permanent);
                    }
                }
            }
        }

        for (Permanent permanent : toExile) {
            permanentRemovalService.removePermanentToExile(gameData, permanent);
            gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is exiled."));
            log.info("Game {} - {} exiles {}", gameData.id, entry.getCard().getName(),
                    permanent.getCard().getName());
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
