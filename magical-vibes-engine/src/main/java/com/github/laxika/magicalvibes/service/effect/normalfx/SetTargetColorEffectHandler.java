package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.SetTargetColorEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetTargetColorEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetTargetColorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SetTargetColorEffect) effect;
        UUID targetId = entry.getTargetId();
        String colorName = e.color().name().charAt(0) + e.color().name().substring(1).toLowerCase();

        // Target may be a permanent or, like Glamerdye, a spell still on the stack.
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target != null) {
            // CR 105.3 / 611.2b: "becomes [color]" with no stated duration is a floating layer-5 color
            // setter that replaces all previous colors and lasts indefinitely (EffectDuration.PERMANENT).
            // The legacy fields are seeded for direct getEffectiveColor callers; the layered pass replays
            // the floating effect at its real timestamp.
            target.getTransientColors().clear();
            target.getTransientColors().add(e.color());
            target.setColorOverridden(true);
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                    entry.getCard().getName(), null, entry.getControllerId(), e,
                    target.getId(), null, null, EffectDuration.PERMANENT, 0));

            String logEntry = target.getCard().getName() + " becomes " + colorName + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} becomes {}", gameData.id, target.getCard().getName(), colorName);
            return;
        }

        // A spell target: record the color so it carries onto the permanent that spell resolves into
        // (CR 613.7). For instants/sorceries it is a no-op.
        StackEntry targetSpell = gameQueryService.findStackEntryByCardId(gameData, targetId);
        if (targetSpell != null) {
            gameData.spellColorOverrides.put(targetId, e.color());
            String logEntry = targetSpell.getCard().getName() + " becomes " + colorName + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - spell {} becomes {}", gameData.id, targetSpell.getCard().getName(), colorName);
        }
    }
}
