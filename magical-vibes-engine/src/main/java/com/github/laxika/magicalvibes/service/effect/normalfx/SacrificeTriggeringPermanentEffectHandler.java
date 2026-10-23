package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SacrificeTriggeringPermanentEffect}: sacrifices the triggering permanent when the
 * ability's controller still controls it (CR 701.21a). Never targets, so an absent permanent is a
 * silent no-op.
 */
@Component
@RequiredArgsConstructor
public class SacrificeTriggeringPermanentEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeTriggeringPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID triggeringId = entry.getTriggeringPermanentId();
        if (triggeringId == null) {
            return;
        }
        Permanent triggering = gameQueryService.findPermanentById(gameData, triggeringId);
        if (triggering == null) {
            return;
        }
        if (!Objects.equals(entry.getControllerId(),
                gameQueryService.findPermanentController(gameData, triggeringId))) {
            return;
        }

        permanentRemovalService.sacrificePermanentToGraveyard(gameData, triggering);
        gameLogService.append(gameData, GameLog.builder()
                .card(triggering.getCard())
                .text(" is sacrificed.")
                .build());
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
