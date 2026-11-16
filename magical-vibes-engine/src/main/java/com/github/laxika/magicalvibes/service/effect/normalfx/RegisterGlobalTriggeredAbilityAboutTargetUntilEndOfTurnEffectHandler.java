package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityAboutTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Stores a delayed triggered ability about the registering ability's target creature until turn cleanup. It records
 * the registering source permanent too, which is its source (CR 603.7e).
 */
@Component
@RequiredArgsConstructor
public class RegisterGlobalTriggeredAbilityAboutTargetUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterGlobalTriggeredAbilityAboutTargetUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var registration = (RegisterGlobalTriggeredAbilityAboutTargetUntilEndOfTurnEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        if (targetIds.isEmpty() || gameQueryService.findPermanentById(gameData, targetIds.getFirst()) == null) {
            return;
        }
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.about(
                entry.getControllerId(), entry.getCard(), registration.ability(), targetIds.getFirst(),
                entry.getSourcePermanentId(), true));
    }
}
