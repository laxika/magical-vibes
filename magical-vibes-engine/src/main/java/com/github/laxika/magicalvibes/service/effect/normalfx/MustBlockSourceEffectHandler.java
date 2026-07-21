package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MustBlockSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MustBlockSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MustBlockSourceEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        // Activated abilities and combat triggers snapshot the source onto the effect; targeted-ETB
        // "may" triggers (Giant Ambush Beetle) leave it null and carry the source on the stack entry.
        UUID sourceId = e.sourcePermanentId() != null ? e.sourcePermanentId() : entry.getSourcePermanentId();
        if (target == null || sourceId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        String sourceName = source != null ? source.getCard().getName() : entry.getCard().getName();

        target.getMustBlockIds().add(sourceId);

        String logEntry = target.getCard().getName() + " must block " + sourceName + " this turn if able.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(target.getCard()).text(" must block " + sourceName + " this turn if able.").build());

        log.info("Game {} - {} must block {} this turn if able", gameData.id, target.getCard().getName(), sourceName);
    }
}
