package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GiveControllerRadCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a controller rad-counter rider on a mana ability. */
@Component
public class GiveControllerRadCountersManaAbilityEffectHandler implements ManaAbilityEffectHandler {

    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;

    public GiveControllerRadCountersManaAbilityEffectHandler(AmountEvaluationService amountEvaluationService,
                                                              GameLogService gameLogService) {
        this.amountEvaluationService = amountEvaluationService;
        this.gameLogService = gameLogService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GiveControllerRadCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        CardEffect effect, int manaMultiplier, boolean creatureSource) {
        GiveControllerRadCountersEffect radCounters = (GiveControllerRadCountersEffect) effect;
        int amount = amountEvaluationService.evaluate(gameData, radCounters.amount(),
                AmountContext.forManaAbility(permanent, playerId));
        if (amount <= 0) {
            return;
        }

        gameData.playerRadCounters.merge(playerId, amount, Integer::sum);
        String playerName = gameData.playerIdToName.getOrDefault(playerId, player.getUsername());
        gameLogService.append(gameData,
                GameLog.text(playerName + " gets " + amount + " rad counter" + (amount == 1 ? "." : "s.")));
    }
}
