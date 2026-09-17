package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndTargetPlayerLosesLifeByMilledManaValueEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a mill followed by life loss based on the mana values of the milled cards. */
@Component
@RequiredArgsConstructor
public class MillControllerAndTargetPlayerLosesLifeByMilledManaValueEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerAndTargetPlayerLosesLifeByMilledManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var mill = (MillControllerAndTargetPlayerLosesLifeByMilledManaValueEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }

        int count = Math.max(0, amountEvaluationService.evaluate(
                gameData, mill.count(), AmountContext.forStackEntry(entry, source)));
        List<Card> milled = graveyardService.resolveMillPlayer(gameData, entry.getControllerId(), count);
        int totalManaValue = milled.stream().mapToInt(Card::getManaValue).sum();

        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }
        lifeSupport.applyLifeLoss(gameData, targetPlayerId, totalManaValue, entry.getCard().getName());
    }
}
