package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutTargetCreatureUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves the source-linked phase-out used by Oubliette. */
@Component
@RequiredArgsConstructor
public class PhaseOutTargetCreatureUntilSourceLeavesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PhasingService phasingService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PhaseOutTargetCreatureUntilSourceLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PhaseOutTargetCreatureUntilSourceLeavesEffect) effect;
        Permanent source = findSource(gameData, entry.getSourcePermanentId());
        UUID targetId = entry.targetsForEffect(effect).stream().findFirst().orElse(entry.getTargetId());
        Permanent target = findPermanent(gameData, targetId);
        if (source != null && target != null) {
            phasingService.phaseOutUntilSourceLeaves(gameData, source, target, e.tapsOnPhaseIn());
        }
    }

    /** A phased-out source has not left the battlefield, so the trigger still links to it (CR 113.7a). */
    private Permanent findSource(GameData gameData, UUID sourceId) {
        Permanent source = findPermanent(gameData, sourceId);
        if (source == null && sourceId != null) {
            source = phasingService.findPhasedOutPermanent(gameData, sourceId);
        }
        return source;
    }

    private Permanent findPermanent(GameData gameData, UUID permanentId) {
        return permanentId == null ? null : gameQueryService.findPermanentById(gameData, permanentId);
    }
}
