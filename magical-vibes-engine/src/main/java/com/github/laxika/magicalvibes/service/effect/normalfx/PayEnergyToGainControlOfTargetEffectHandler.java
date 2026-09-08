package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyToGainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayEnergyToGainControlOfTargetEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final CreatureControlService creatureControlService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayEnergyToGainControlOfTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PayEnergyToGainControlOfTargetEffect payEffect = (PayEnergyToGainControlOfTargetEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
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

        creatureControlService.applyControlEffect(
                gameData,
                entry.getControllerId(),
                target,
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                EffectDuration.PERMANENT,
                null,
                entry.getCard().getName());
    }
}
