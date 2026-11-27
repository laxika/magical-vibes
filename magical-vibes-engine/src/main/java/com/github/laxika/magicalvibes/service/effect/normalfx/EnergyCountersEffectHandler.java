package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PlayerCounterType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "You get {E}" ({@link EnergyCountersEffect}): the ability's controller puts the energy counters on themselves through
 * {@link LifeSupport#givePlayerCounters}, its lock, replacements, log and "you put counters" event (All Will Be One);
 * a negative amount removes that many, to none.
 */
@Component
@RequiredArgsConstructor
public class EnergyCountersEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnergyCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EnergyCountersEffect energy = (EnergyCountersEffect) effect;
        int amount = amountEvaluationService.evaluate(gameData, energy.amount(),
                AmountContext.forStackEntry(entry, null));
        if (amount == 0) {
            return;
        }
        if (amount > 0) {
            // "You get" is put by the ability's controller (CR 608.2c).
            lifeSupport.givePlayerCounters(gameData, entry.getControllerId(), PlayerCounterType.ENERGY, amount,
                    entry.getCard().getName(), entry.getControllerId());
            return;
        }

        int current = gameData.playerEnergyCounters.getOrDefault(entry.getControllerId(), 0);
        int updated = Math.max(0, current + amount);
        int changed = updated - current;
        if (changed == 0) {
            return;
        }
        gameData.setPlayerEnergyCounters(entry.getControllerId(), updated);
        String playerName = gameData.playerIdToName.getOrDefault(entry.getControllerId(), "Player");
        gameLogService.append(gameData, GameLog.text(playerName + " pays " + -changed + " energy counter(s)."));
    }
}
