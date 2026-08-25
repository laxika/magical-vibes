package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSubject;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PhaseOutEffect}: resolves the subject permanent, then hands it to
 * {@link PhasingService}, which owns attachments (CR 702.26g), combat removal (CR 506.4) and
 * phasing back in. If the subject is gone — the source or target already left the battlefield, or
 * the source Aura is unattached — the ability simply does nothing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PhaseOutEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PhasingService phasingService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PhaseOutEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PhaseOutEffect e = (PhaseOutEffect) effect;

        List<UUID> targetIds = e.subject() == PhaseOutSubject.TARGET
                ? entry.targetsForEffect(effect)
                : List.of();
        if (e.subject() == PhaseOutSubject.TARGET && targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        if (e.subject() == PhaseOutSubject.TARGET) {
            List<Permanent> subjects = targetIds.stream()
                    .map(id -> findPermanent(gameData, id))
                    .filter(java.util.Objects::nonNull)
                    .toList();
            if (!subjects.isEmpty()) {
                phasingService.phaseOut(gameData, subjects);
            }
            return;
        }

        Permanent subject = switch (e.subject()) {
            case SOURCE -> findPermanent(gameData, entry.getSourcePermanentId());
            case TARGET -> null;
            case ATTACHED -> findAttached(gameData, entry);
        };
        if (subject != null) {
            phasingService.phaseOut(gameData, List.of(subject));
        }
    }

    private Permanent findPermanent(GameData gameData, UUID permanentId) {
        return permanentId == null ? null : gameQueryService.findPermanentById(gameData, permanentId);
    }

    private Permanent findAttached(GameData gameData, StackEntry entry) {
        Permanent aura = findPermanent(gameData, entry.getSourcePermanentId());
        if (aura == null) {
            log.info("Game {} - Aura {} no longer on battlefield, skipping phase out",
                    gameData.id, entry.getCard().getName());
            return null;
        }

        UUID attachedToId = aura.getAttachedTo();
        if (attachedToId == null) {
            log.info("Game {} - {} is not attached to anything, skipping phase out",
                    gameData.id, entry.getCard().getName());
            return null;
        }

        Permanent host = gameQueryService.findPermanentById(gameData, attachedToId);
        if (host == null) {
            log.info("Game {} - Enchanted creature no longer on battlefield, skipping phase out",
                    gameData.id);
        }
        return host;
    }
}
