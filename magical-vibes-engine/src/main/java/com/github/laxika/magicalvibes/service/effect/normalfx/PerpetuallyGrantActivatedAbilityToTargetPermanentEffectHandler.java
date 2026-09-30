package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantActivatedAbilityToTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Records a perpetual activated-ability grant on targeted permanent card identities. */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantActivatedAbilityToTargetPermanentEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantActivatedAbilityToTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PerpetuallyGrantActivatedAbilityToTargetPermanentEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            ActivatedAbility ability = grant.ability();
            UUID cardId = target.getCard().getId();
            gameData.perpetualActivatedAbilities.compute(cardId, (ignored, existing) -> {
                List<ActivatedAbility> updated = new ArrayList<>(existing == null ? List.of() : existing);
                if (!updated.contains(ability)) {
                    updated.add(ability);
                }
                return List.copyOf(updated);
            });
            if (!target.getPersistentGrantedActivatedAbilities().contains(ability)) {
                target.getPersistentGrantedActivatedAbilities().add(ability);
            }
        }
    }
}
