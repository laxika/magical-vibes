package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordsToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Records perpetual keyword and triggered-ability grants on targeted creature card identities. */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantKeywordsToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantKeywordsToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PerpetuallyGrantKeywordsToTargetCreatureEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            Set<Keyword> grantable = grant.keywords().stream()
                    .filter(keyword -> !gameQueryService.cantHaveOrGainKeyword(gameData, target, keyword))
                    .collect(java.util.stream.Collectors.toCollection(() -> EnumSet.noneOf(Keyword.class)));
            if (grantable.isEmpty()) {
                continue;
            }

            UUID cardId = target.getCard().getId();
            gameData.perpetualCardKeywords
                    .computeIfAbsent(cardId, ignored -> EnumSet.noneOf(Keyword.class))
                    .addAll(grantable);

            if (grant.triggeredAbilitySlot() != null) {
                addPerpetualTriggeredAbility(gameData, cardId, grant.triggeredAbilitySlot(),
                        grant.triggeredAbility());
                if (!target.getPersistentTriggeredEffects(grant.triggeredAbilitySlot())
                        .contains(grant.triggeredAbility())) {
                    target.addPersistentTriggeredEffect(grant.triggeredAbilitySlot(),
                            grant.triggeredAbility());
                }
            }
        }
    }

    private void addPerpetualTriggeredAbility(GameData gameData, UUID cardId, EffectSlot slot,
                                               CardEffect triggeredAbility) {
        gameData.perpetualTriggeredAbilityGrants.compute(cardId, (ignored, existing) -> {
            Map<EffectSlot, List<CardEffect>> updated = new EnumMap<>(EffectSlot.class);
            if (existing != null) {
                existing.forEach((existingSlot, effects) ->
                        updated.put(existingSlot, new ArrayList<>(effects)));
            }
            List<CardEffect> effects = updated.computeIfAbsent(slot, ignoredSlot -> new ArrayList<>());
            if (!effects.contains(triggeredAbility)) {
                effects.add(triggeredAbility);
            }
            updated.replaceAll((existingSlot, slotEffects) -> List.copyOf(slotEffects));
            return Map.copyOf(updated);
        });
    }
}
