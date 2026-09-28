package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.GrantingPermanentAwareEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantEffectToTargetUntilEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantEffectToTargetUntilEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantEffectToTargetUntilEndOfCombatEffect) effect;

        if (entry.getTargetIds() != null && !entry.getTargetIds().isEmpty()) {
            for (UUID targetId : entry.getTargetIds()) {
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target != null) {
                    grantTo(gameData, entry, grant, target);
                }
            }
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            log.info("Game {} - Target creature no longer on battlefield, effect fizzles", gameData.id);
            return;
        }
        grantTo(gameData, entry, grant, target);
    }

    private void grantTo(GameData gameData, StackEntry entry,
                         GrantEffectToTargetUntilEndOfCombatEffect grant, Permanent target) {
        CardEffect grantedEffect = grant.grantedEffect();
        if (grantedEffect instanceof GrantingPermanentAwareEffect aware
                && entry.getSourcePermanentId() != null) {
            grantedEffect = aware.withGrantingPermanentId(entry.getSourcePermanentId());
        }
        target.addCombatTriggeredEffect(grant.slot(), grantedEffect);

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" grants a temporary " + grant.slot().name() + " ability to ")
                .card(target.getCard())
                .text(" for this combat.")
                .build());
        log.info("Game {} - {} grants temporary {} effect to {} for this combat",
                gameData.id, entry.getCard().getName(), grant.slot().name(), target.getCard().getName());
    }
}
