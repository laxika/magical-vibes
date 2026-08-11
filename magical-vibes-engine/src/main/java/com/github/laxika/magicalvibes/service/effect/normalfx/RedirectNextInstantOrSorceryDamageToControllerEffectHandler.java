package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectNextInstantOrSorceryDamageToControllerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves the one-shot damage redirection shield created by Aegis of Honor. */
@Component
@RequiredArgsConstructor
public class RedirectNextInstantOrSorceryDamageToControllerEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectNextInstantOrSorceryDamageToControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getControllerId();
        if (playerId == null) {
            return;
        }

        gameData.playerNextInstantOrSorceryDamageRedirectShields.add(playerId);
        gameLogService.append(gameData, GameLog.text(
                "The next time an instant or sorcery spell would deal damage to "
                        + gameData.playerIdToName.get(playerId)
                        + " this turn, that spell deals that damage to its controller instead."));
    }
}
