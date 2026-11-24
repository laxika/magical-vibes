package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.UnattachEquipmentFromTargetPermanentsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.duration.DurationClock;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Unattaches the effect's Equipment (CR 701.3d): every Equipment attached to its targets, or the Equipment its subject
 * names. A permanent that isn't an Equipment as the effect resolves, or is attached to nothing, is left alone; an
 * unattached one stays on the battlefield, the effects lasting while it stays attached end and its "becomes
 * unattached" event occurs.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnattachEquipmentFromTargetPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final DurationClock durationClock;
    private final GameLogService gameLogService;
    private final UnattachTriggerSupport unattachTriggerSupport;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UnattachEquipmentFromTargetPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var unattach = (UnattachEquipmentFromTargetPermanentsEffect) effect;
        List<Permanent> equipment = unattach.equipment() == null
                ? attachedToTargets(gameData, entry, effect)
                : subjectResolutionService.permanents(gameData, entry, effect, unattach.equipment());
        for (Permanent p : equipment) {
            UUID attachedTo = p.getAttachedTo();
            if (attachedTo == null || !gameQueryService.hasEffectiveSubtype(gameData, p, CardSubtype.EQUIPMENT)) {
                continue;
            }
            Permanent host = gameQueryService.findPermanentById(gameData, attachedTo);
            unattachTriggerSupport.triggerDestroyOnUnattachIfNeeded(gameData, p, attachedTo);
            p.setAttachedTo(null);
            durationClock.sourceUnattached(gameData, p.getId());
            var line = GameLog.builder().card(entry.getCard()).text(" unattaches ").card(p.getCard());
            if (host != null) {
                line.text(" from ").card(host.getCard());
            }
            gameLogService.append(gameData, line.text(".").build());
            log.info("Game {} - {} unattaches {}", gameData.id, entry.getCard().getName(), p.getCard().getName());
        }
    }

    /** Every Equipment attached to one of the effect's targets still on the battlefield. */
    private List<Permanent> attachedToTargets(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty()) {
            targetIds = entry.getTargetIds();
        }
        if (targetIds == null || targetIds.isEmpty()) {
            targetIds = entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId());
        }
        List<Permanent> attached = new ArrayList<>();
        for (UUID targetId : targetIds) {
            if (gameQueryService.findPermanentById(gameData, targetId) == null) {
                continue;
            }
            gameData.forEachPermanent((playerId, p) -> {
                if (targetId.equals(p.getAttachedTo())) {
                    attached.add(p);
                }
            });
        }
        return attached;
    }
}
