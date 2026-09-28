package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyThenDestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PayEnergyThenDestroyTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayEnergyThenDestroyTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PayEnergyThenDestroyTargetPermanentEffect payEffect =
                (PayEnergyThenDestroyTargetPermanentEffect) effect;
        List<UUID> effectTargets = entry.targetsForEffect(payEffect);
        UUID targetId = effectTargets.isEmpty() ? entry.getTargetId() : effectTargets.getFirst();
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        int energyAmount = Math.max(0, amountEvaluationService.evaluate(
                gameData, payEffect.energyAmount(), AmountContext.forStackEntry(entry, null)));
        int currentEnergy = gameData.playerEnergyCounters.getOrDefault(entry.getControllerId(), 0);
        if (currentEnergy < energyAmount) {
            return;
        }

        if (energyAmount > 0) {
            gameData.playerEnergyCounters.put(entry.getControllerId(), currentEnergy - energyAmount);
            String playerName = gameData.playerIdToName.getOrDefault(entry.getControllerId(), "Player");
            gameLogService.append(gameData,
                    GameLog.text(playerName + " pays " + energyAmount + " energy counter(s)."));
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (controllerId != null) {
            entry.getRemovedPermanentControllers().put(target.getId(), controllerId);
        }
        entry.rememberLastKnownPermanentCard(target.getId(), target.getCard());
        destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName(), false);
    }
}
