package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantEffectToTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantEffectToTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantEffectToTargetEffect) effect;

        if (e.scope() == GrantScope.ENCHANTED_PERMANENT) {
            Permanent aura = entry.getSourcePermanentId() == null ? null
                    : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            Permanent enchanted = aura == null || aura.getAttachedTo() == null ? null
                    : gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
            if (enchanted != null) {
                grantTo(gameData, entry, e, enchanted);
            }
            return;
        }

        if (e.scope() == GrantScope.SELF) {
            UUID sourcePermanentId = entry.getSourcePermanentId() != null
                    ? entry.getSourcePermanentId() : entry.getTargetId();
            Permanent source = sourcePermanentId == null
                    ? null : gameQueryService.findPermanentById(gameData, sourcePermanentId);
            if (source != null) {
                grantTo(gameData, entry, e, source);
            }
            return;
        }

        List<UUID> ids = entry.targetsForEffect(effect);
        if (ids.isEmpty() && entry.getTargetId() != null) {
            ids = List.of(entry.getTargetId());
        }
        if (ids.isEmpty() && entry.getTargetIds() != null) {
            ids = entry.getTargetIds();
        }

        for (UUID targetId : ids) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }
            grantTo(gameData, entry, e, target);
        }
    }

    private void grantTo(GameData gameData, StackEntry entry, GrantEffectToTargetEffect e, Permanent target) {
        // "If it doesn't have [ability], it gains that ability" — grant at most once (Musician).
        // Some abilities intentionally add another instance each time they resolve (Bushido).
        if (e.skipIfAlreadyPresent() && alreadyHasGrantedEffect(target, e.slot(), e.grantedEffect())) {
            return;
        }

        if (e.duration() == EffectDuration.UNTIL_END_OF_TURN) {
            target.addTemporaryTriggeredEffect(e.slot(), e.grantedEffect());
        } else if (e.duration() == EffectDuration.UNTIL_CONTROLLERS_NEXT_UPKEEP) {
            target.addUntilNextUpkeepTriggeredEffect(entry.getControllerId(), e.slot(), e.grantedEffect());
        } else {
            target.addPersistentTriggeredEffect(e.slot(), e.grantedEffect());
        }

        String durationText = switch (e.duration()) {
            case UNTIL_END_OF_TURN -> " until end of turn";
            case UNTIL_CONTROLLERS_NEXT_UPKEEP -> " until your next upkeep";
            default -> "";
        };
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" grants a " + e.slot().name() + " ability to ")
                .card(target.getCard())
                .text(durationText + ".")
                .build());
        log.info("Game {} - {} grants {} effect to {}{}",
                gameData.id, entry.getCard().getName(), e.slot().name(),
                target.getCard().getName(), durationText);
    }

    private static boolean alreadyHasGrantedEffect(Permanent target, EffectSlot slot, CardEffect granted) {
        if (target.getCard().getEffects(slot).contains(granted)) {
            return true;
        }
        if (target.getPersistentTriggeredEffects(slot).contains(granted)) {
            return true;
        }
        return target.getTemporaryTriggeredEffects(slot).contains(granted);
    }
}
