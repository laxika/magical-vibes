package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreaturePerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoostTargetCreaturePerControlledPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostTargetCreaturePerControlledPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (BoostTargetCreaturePerControlledPermanentEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        int count = 0;
        if (battlefield != null) {
            FilterContext filterContext = FilterContext.of(gameData).withSourceControllerId(controllerId);
            for (Permanent permanent : battlefield) {
                if (gameQueryService.matchesPermanentPredicate(permanent, boost.filter(), filterContext)) {
                    count++;
                }
            }
        }

        int powerBoost = count * boost.powerPerPermanent();
        int toughnessBoost = count * boost.toughnessPerPermanent();
        target.setPowerModifier(target.getPowerModifier() + powerBoost);
        target.setToughnessModifier(target.getToughnessModifier() + toughnessBoost);

        String logEntry = target.getCard().getName() + " gets +" + powerBoost + "/+" + toughnessBoost
                + " until end of turn (" + count + " creature(s)).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} gets +{}/+{} from {} creature(s)",
                gameData.id, target.getCard().getName(), powerBoost, toughnessBoost, count);
    }
}
