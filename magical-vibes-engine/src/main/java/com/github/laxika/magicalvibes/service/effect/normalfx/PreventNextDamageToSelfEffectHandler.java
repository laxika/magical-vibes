package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageToSelfEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PreventNextDamageToSelfEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventNextDamageToSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var prevent = (PreventNextDamageToSelfEffect) effect;
        UUID sourceId = entry.getSourcePermanentId();
        // Without the source creature on the battlefield the ability does nothing.
        if (sourceId == null) return;

        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null) return;

        source.setDamagePreventionShield(source.getDamagePreventionShield() + prevent.amount());

        String logEntry = "The next " + prevent.amount() + " damage that would be dealt to "
                + source.getCard().getName() + " this turn is prevented.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - Self prevention shield {} added to permanent {}", gameData.id, prevent.amount(),
                source.getCard().getName());
    }
}
