package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureToHandAndPerpetuallyBoostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnTargetCreatureToHandAndPerpetuallyBoostEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCreatureToHandAndPerpetuallyBoostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnTargetCreatureToHandAndPerpetuallyBoostEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(e);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            var card = target.getCard();
            if (!permanentRemovalService.removePermanentToHand(gameData, target)) {
                continue;
            }
            gameData.perpetualCardPowerToughnessModifiers.merge(
                    card.getId(),
                    new GameData.PerpetualPowerToughnessModifier(e.powerBoost(), e.toughnessBoost()),
                    (oldValue, newValue) -> new GameData.PerpetualPowerToughnessModifier(
                            oldValue.power() + newValue.power(), oldValue.toughness() + newValue.toughness()));
            gameLogService.append(gameData, GameLog.cardThen(card,
                    " is returned to its owner's hand and perpetually gets "
                            + formatModifier(e.powerBoost(), e.toughnessBoost()) + "."));
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private static String formatModifier(int power, int toughness) {
        return String.format("%+d/%+d", power, toughness);
    }
}
