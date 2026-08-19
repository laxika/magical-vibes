package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromTargetPermanentColorsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GrantProtectionFromTargetPermanentColorsUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantProtectionFromTargetPermanentColorsUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (target == null || battlefield == null) {
            return;
        }

        Set<CardColor> colors = Set.copyOf(gameQueryService.getEffectiveColors(gameData, target));
        if (colors.isEmpty()) {
            return;
        }

        for (Permanent permanent : battlefield) {
            if (!gameQueryService.isCreature(gameData, permanent)) {
                continue;
            }
            permanent.getProtectionFromColorsUntilEndOfTurn().addAll(colors);
            gameLogService.append(gameData, GameLog.builder()
                    .card(permanent.getCard())
                    .text(" gains protection from the colors of the target permanent until end of turn.")
                    .build());
        }
    }
}
