package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.SetSelfKeywordIndefinitelyEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetSelfKeywordIndefinitelyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetSelfKeywordIndefinitelyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var change = (SetSelfKeywordIndefinitelyEffect) effect;
        if (entry.getSourcePermanentId() == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        // The gain and lose abilities are opposites of each other, so a new activation supersedes
        // whatever the previous one established for this keyword instead of stacking behind it.
        gameData.floatingEffects.removeIf(floating ->
                floating.effect() instanceof SetSelfKeywordIndefinitelyEffect previous
                        && previous.keyword() == change.keyword()
                        && source.getId().equals(floating.affectedPermanentId()));

        // CR 611.2b: no stated duration means the continuous effect lasts indefinitely.
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                entry.getCard().getName(), source.getId(), entry.getControllerId(), change,
                source.getId(), null, null, EffectDuration.PERMANENT, 0));

        gameLogService.append(gameData, GameLog.builder()
                .card(source.getCard())
                .text(String.format(" %s %s indefinitely.", change.gained() ? "gains" : "loses",
                        change.keyword().name().toLowerCase().replace('_', ' ')))
                .build());
        log.info("Game {} - {} {} {} indefinitely", gameData.id, source.getCard().getName(),
                change.gained() ? "gains" : "loses", change.keyword());
    }
}
