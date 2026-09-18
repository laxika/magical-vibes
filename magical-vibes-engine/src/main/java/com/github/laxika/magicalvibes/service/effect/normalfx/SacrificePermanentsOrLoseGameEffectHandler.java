package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsOrLoseGameEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SacrificePermanentsOrLoseGameEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;
    private final SacrificePermanentsEffectHandler sacrificePermanentsEffectHandler;
    private final ControllerLosesGameEffectHandler controllerLosesGameEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificePermanentsOrLoseGameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var sacrifice = (SacrificePermanentsOrLoseGameEffect) effect;
        if (entry.getControllerId() == null || !gameData.playerIds.contains(entry.getControllerId())) {
            return;
        }
        var source = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int count = amountEvaluationService.evaluate(gameData, sacrifice.count(),
                AmountContext.forStackEntry(entry, source != null ? source : entry.getSourcePermanentSnapshot()));
        if (count <= 0) {
            return;
        }
        var delegated = new SacrificePermanentsEffect(count,
                new PermanentAllOfPredicate(List.of(sacrifice.filter())), SacrificeRecipient.CONTROLLER)
                .withSimultaneousChoices();
        boolean canSacrifice = sacrificePermanentsEffectHandler.hasLegalSacrificeChoice(
                gameData, entry, delegated, entry.getControllerId());
        sacrificePermanentsEffectHandler.resolve(gameData, entry, delegated);
        if (!canSacrifice) {
            controllerLosesGameEffectHandler.resolve(gameData, entry, new ControllerLosesGameEffect());
        }
    }
}
