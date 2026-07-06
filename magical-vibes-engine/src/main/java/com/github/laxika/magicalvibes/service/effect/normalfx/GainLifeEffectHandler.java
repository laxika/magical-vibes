package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GainLifeEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GainLifeEffect) effect;
        // Source-relative amounts use the live source permanent when it is still on the
        // battlefield, else the last-known snapshot (e.g. sacrificed as an activation cost).
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));

        if (e.recipient() == GainLifeRecipient.TARGET_CONTROLLER) {
            // "its controller gains life …" (e.g. Condemn): route to the target permanent's
            // controller. No legal target -> no one gains, matching the effect's fizzle behaviour.
            UUID targetControllerId = controllerOfTarget(gameData, entry.getTargetId());
            if (targetControllerId != null) {
                lifeSupport.applyGainLife(gameData, targetControllerId, amount);
            }
            return;
        }

        lifeSupport.applyGainLife(gameData, entry.getControllerId(), amount, null,
                entry.getCard(), entry.getEntryType());
    }

    private UUID controllerOfTarget(GameData gameData, UUID targetId) {
        if (targetId == null) return null;
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) return null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.contains(target)) {
                return playerId;
            }
        }
        return null;
    }
}
