package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyExchangeTargetCreatureBasePowerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Applies a perpetual base-power exchange to two targeted creature cards. */
@Component
@RequiredArgsConstructor
public class PerpetuallyExchangeTargetCreatureBasePowerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyExchangeTargetCreatureBasePowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PerpetuallyExchangeTargetCreatureBasePowerEffect exchange =
                (PerpetuallyExchangeTargetCreatureBasePowerEffect) effect;
        int targetGroup = entry.getCard().getEffectTargetIndex(exchange);
        List<java.util.UUID> targetIds = targetGroup >= 0
                ? entry.targetsForGroup(targetGroup) : entry.getTargetIds();
        if (targetIds.size() < 2) {
            return;
        }

        Permanent first = gameQueryService.findPermanentById(gameData, targetIds.get(0));
        Permanent second = gameQueryService.findPermanentById(gameData, targetIds.get(1));
        if (first == null || second == null
                || !gameQueryService.isCreature(gameData, first)
                || !gameQueryService.isCreature(gameData, second)) {
            return;
        }

        int firstPower = gameQueryService.getEffectiveBasePower(gameData, first);
        int secondPower = gameQueryService.getEffectiveBasePower(gameData, second);
        replacePower(gameData, first, secondPower);
        replacePower(gameData, second, firstPower);

        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text(": perpetually exchanges the base power of ")
                .card(first.getCard()).text(" and ").card(second.getCard()).text(".").build());
    }

    /**
     * Records the new base power as a perpetual layer 7b setter, so it also overrides a
     * characteristic-defining power (CR 613.4a/b) while the toughness stays whatever it was.
     */
    private void replacePower(GameData gameData, Permanent permanent, int power) {
        var existing = gameData.perpetualCardBasePowerToughness.get(permanent.getCard().getId());
        gameData.perpetualCardBasePowerToughness.put(permanent.getCard().getId(),
                new GameData.PerpetualBasePowerToughness(power,
                        existing == null ? null : existing.toughness(), gameData.nextTimestamp()));
    }
}
