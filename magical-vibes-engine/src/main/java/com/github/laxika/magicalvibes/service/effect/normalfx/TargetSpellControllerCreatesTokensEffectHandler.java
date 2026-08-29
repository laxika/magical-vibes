package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerCreatesTokensEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TargetSpellControllerCreatesTokensEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetSpellControllerCreatesTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var tokenEffect = ((TargetSpellControllerCreatesTokensEffect) effect).tokenEffect();
        UUID targetSpellControllerId = findTargetSpellControllerId(gameData, entry.getTargetId());
        if (targetSpellControllerId == null) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }

        AmountContext context = AmountContext.forStackEntry(entry, source);
        int amount = amountEvaluationService.evaluate(gameData, tokenEffect.amount(), context);
        if (amount <= 0) {
            return;
        }
        int power = amountEvaluationService.evaluate(gameData, tokenEffect.power(), context);
        int toughness = amountEvaluationService.evaluate(gameData, tokenEffect.toughness(), context);
        entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                gameData, targetSpellControllerId, tokenEffect, amount,
                entry.getCard().getSetCode(), power, toughness));
    }

    private UUID findTargetSpellControllerId(GameData gameData, UUID targetCardId) {
        if (targetCardId == null) {
            return null;
        }
        for (StackEntry stackEntry : gameData.stack) {
            if (stackEntry.getCard().getId().equals(targetCardId)) {
                return stackEntry.getControllerId();
            }
        }
        return null;
    }
}
