package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToOwnCreaturesEffect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Records a triggered ability on each creature currently controlled by the source's controller. */
@Component
public class PerpetuallyGrantTriggeredAbilityToOwnCreaturesEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantTriggeredAbilityToOwnCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var perpetual = (PerpetuallyGrantTriggeredAbilityToOwnCreaturesEffect) effect;
        List<Permanent> battlefield = gameData.playerBattlefields
                .getOrDefault(entry.getControllerId(), List.of());
        for (Permanent permanent : battlefield) {
            if (!permanent.getCard().hasType(CardType.CREATURE)
                    || permanent.getOriginalCard() == null) {
                continue;
            }

            UUID cardId = permanent.getOriginalCard().getId();
            gameData.perpetualTriggeredAbilityGrants.compute(cardId, (ignored, existing) -> {
                Map<EffectSlot, List<CardEffect>> updated = new EnumMap<>(EffectSlot.class);
                if (existing != null) {
                    existing.forEach((slot, effects) -> updated.put(slot, new ArrayList<>(effects)));
                }
                List<CardEffect> effects = updated.computeIfAbsent(perpetual.triggeredAbilitySlot(),
                        ignoredSlot -> new ArrayList<>());
                if (!effects.contains(perpetual.triggeredAbility())) {
                    effects.add(perpetual.triggeredAbility());
                }
                updated.replaceAll((slot, slotEffects) -> List.copyOf(slotEffects));
                return Map.copyOf(updated);
            });

            if (!permanent.getPersistentTriggeredEffects(perpetual.triggeredAbilitySlot())
                    .contains(perpetual.triggeredAbility())) {
                permanent.addPersistentTriggeredEffect(
                        perpetual.triggeredAbilitySlot(), perpetual.triggeredAbility());
            }
        }
    }
}
