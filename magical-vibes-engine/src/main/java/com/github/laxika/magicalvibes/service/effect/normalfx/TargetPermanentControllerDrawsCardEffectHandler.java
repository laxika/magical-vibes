package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPermanentControllerDrawsCardEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TargetPermanentControllerDrawsCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final DrawService drawService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPermanentControllerDrawsCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPermanentControllerDrawsCardEffect) effect;
        if (entry.getTargetId() == null) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, entry.getTargetId());
        if (controllerId != null) {
            Permanent source = entry.getSourcePermanentId() != null
                    ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                    : null;
            if (source == null) {
                source = entry.getSourcePermanentSnapshot();
            }
            int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                    AmountContext.forStackEntry(entry, source));
            for (int i = 0; i < amount; i++) {
                drawService.resolveDrawCard(gameData, controllerId);
            }
        }
    }
}
