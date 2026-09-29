package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToSourceCardEffect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Records a perpetual triggered-ability grant on the source card's identity. */
@Component
public class PerpetuallyGrantTriggeredAbilityToSourceCardEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantTriggeredAbilityToSourceCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getCard() == null) {
            return;
        }
        var grant = (PerpetuallyGrantTriggeredAbilityToSourceCardEffect) effect;
        gameData.perpetualTriggeredAbilityGrants.compute(entry.getCard().getId(), (ignored, existing) -> {
            Map<EffectSlot, List<CardEffect>> updated = new EnumMap<>(EffectSlot.class);
            if (existing != null) {
                existing.forEach((slot, effects) -> updated.put(slot, new ArrayList<>(effects)));
            }
            List<CardEffect> effects = updated.computeIfAbsent(grant.triggeredAbilitySlot(),
                    ignoredSlot -> new ArrayList<>());
            if (!effects.contains(grant.triggeredAbility())) {
                effects.add(grant.triggeredAbility());
            }
            updated.replaceAll((slot, slotEffects) -> List.copyOf(slotEffects));
            return Map.copyOf(updated);
        });
    }
}
