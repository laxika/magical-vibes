package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfEnchantedTargetEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GainControlOfEnchantedTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfEnchantedTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;

        if (!gameQueryService.isEnchanted(gameData, target)) {
            String logEntry = entry.getCard().getName() + "'s ability has no effect (" + target.getCard().getName() + " is not enchanted).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(entry.getCard(), "'s ability has no effect (", target.getCard(), " is not enchanted)."));
            return;
        }

        // "For as long as that creature remains enchanted": no time-based expiry — the
        // condition is enforced by CreatureControlService.reconcileControl, keyed on the
        // wrapped GainControlOfEnchantedTargetEffect type.
        creatureControlService.applyControlEffect(gameData, entry.getControllerId(), target,
                effect, EffectDuration.PERMANENT, null, entry.getCard().getName());
    }
}
