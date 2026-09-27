package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAnyColorManaAndSelfCastAbilityToCardEffect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class PerpetuallyGrantAnyColorManaAndSelfCastAbilityToCardEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantAnyColorManaAndSelfCastAbilityToCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PerpetuallyGrantAnyColorManaAndSelfCastAbilityToCardEffect) effect;
        UUID cardId = grant.cardId();
        if (cardId == null) {
            return;
        }

        gameData.perpetualAnyColorManaForCastCardIds.add(cardId);
        gameData.perpetualTriggeredAbilityGrants.compute(cardId, (ignored, existing) -> {
            Map<EffectSlot, List<CardEffect>> updated = new EnumMap<>(EffectSlot.class);
            if (existing != null) {
                existing.forEach((slot, effects) -> updated.put(slot, new ArrayList<>(effects)));
            }
            List<CardEffect> selfCastEffects = updated.computeIfAbsent(EffectSlot.ON_SELF_CAST,
                    ignoredSlot -> new ArrayList<>());
            if (!selfCastEffects.contains(grant.selfCastAbility())) {
                selfCastEffects.add(grant.selfCastAbility());
            }
            updated.replaceAll((slot, effects) -> List.copyOf(effects));
            return Map.copyOf(updated);
        });
    }
}
