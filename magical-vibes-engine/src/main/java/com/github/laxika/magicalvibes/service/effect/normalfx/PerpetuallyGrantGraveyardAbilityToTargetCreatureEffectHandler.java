package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantGraveyardAbilityToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Records a graveyard ability grant on targeted creature card identities. */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantGraveyardAbilityToTargetCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantGraveyardAbilityToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PerpetuallyGrantGraveyardAbilityToTargetCreatureEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || target.getOriginalCard() == null) {
                continue;
            }

            Card targetCard = target.getOriginalCard();
            UUID cardId = targetCard.getId();
            ActivatedAbility ability = grant.ability();
            gameData.perpetualGraveyardAbilities.compute(cardId, (ignored, existing) -> {
                List<ActivatedAbility> updated = new ArrayList<>(existing == null ? List.of() : existing);
                if (!updated.contains(ability)) {
                    updated.add(ability);
                }
                return List.copyOf(updated);
            });
        }
    }
}
