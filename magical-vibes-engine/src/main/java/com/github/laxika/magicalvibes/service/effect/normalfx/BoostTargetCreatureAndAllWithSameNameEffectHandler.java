package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureAndAllWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BoostTargetCreatureAndAllWithSameNameEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostTargetCreatureAndAllWithSameNameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        var boost = (BoostTargetCreatureAndAllWithSameNameEffect) effect;
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int powerBoost = amountEvaluationService.evaluate(gameData, boost.powerBoost(),
                AmountContext.forStackEntry(entry, source));
        int toughnessBoost = amountEvaluationService.evaluate(gameData, boost.toughnessBoost(),
                AmountContext.forStackEntry(entry, source));

        String targetName = target.getCard().getName();
        List<Permanent> toBoost = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && permanent.getCard().getName().equals(targetName)) {
                    toBoost.add(permanent);
                }
            }
        });

        for (Permanent permanent : toBoost) {
            permanent.setPowerModifier(permanent.getPowerModifier() + powerBoost);
            permanent.setToughnessModifier(permanent.getToughnessModifier() + toughnessBoost);
        }
    }
}
