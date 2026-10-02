package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemovePerpetualTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Removes a previously granted triggered ability from the source card and permanent. */
@Component
@RequiredArgsConstructor
public class RemovePerpetualTriggeredAbilityEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemovePerpetualTriggeredAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var removal = (RemovePerpetualTriggeredAbilityEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        UUID cardId = source != null && source.getOriginalCard() != null
                ? source.getOriginalCard().getId() : entry.getCard().getId();

        gameData.perpetualTriggeredAbilityGrants.computeIfPresent(cardId, (ignored, existing) -> {
            Map<EffectSlot, List<CardEffect>> updated = new EnumMap<>(EffectSlot.class);
            existing.forEach((slot, effects) -> updated.put(slot, new ArrayList<>(effects)));
            List<CardEffect> slotEffects = updated.get(removal.triggeredAbilitySlot());
            if (slotEffects != null) {
                slotEffects.removeIf(removal.triggeredAbilityType()::isInstance);
                if (slotEffects.isEmpty()) {
                    updated.remove(removal.triggeredAbilitySlot());
                }
            }
            return updated.isEmpty() ? null : Map.copyOf(updated);
        });

        if (source != null) {
            List<CardEffect> persistentEffects = source.getPersistentTriggeredEffects(removal.triggeredAbilitySlot());
            if (!persistentEffects.isEmpty()) {
                persistentEffects.removeIf(removal.triggeredAbilityType()::isInstance);
            }
        }
    }
}
