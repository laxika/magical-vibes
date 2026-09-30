package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
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

        int firstPower = first.getBasePower();
        int secondPower = second.getBasePower();
        replacePower(first, secondPower);
        replacePower(second, firstPower);

        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text(": perpetually exchanges the base power of ")
                .card(first.getCard()).text(" and ").card(second.getCard()).text(".").build());
    }

    private void replacePower(Permanent permanent, int power) {
        Card copy = permanent.getCard().createRuntimeCopy();
        copy.setPower(power);
        copy.freeze();
        permanent.exchangeCard(copy);
    }
}
