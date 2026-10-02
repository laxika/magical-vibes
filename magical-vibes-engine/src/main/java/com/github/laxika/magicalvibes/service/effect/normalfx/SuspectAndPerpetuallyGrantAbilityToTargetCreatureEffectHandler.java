package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SuspectAndPerpetuallyGrantAbilityToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves Snarlfang Vermin's self-replicating suspect ability. */
@Component
@RequiredArgsConstructor
public class SuspectAndPerpetuallyGrantAbilityToTargetCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SuspectAndPerpetuallyGrantAbilityToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !gameQueryService.isCreature(gameData, target)) {
                continue;
            }
            if (!target.isSuspected() && !gameQueryService.cantBecomeSuspected(gameData, target)) {
                target.setSuspected(true);
                gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is suspected."));
            }
            perpetuallyGrantAbility(gameData, target,
                    new SuspectAndPerpetuallyGrantAbilityToTargetCreatureEffect());
        }
    }

    private void perpetuallyGrantAbility(GameData gameData, Permanent target,
                                         CardEffect grantedAbility) {
        if (target.getOriginalCard() == null) {
            return;
        }
        UUID cardId = target.getOriginalCard().getId();
        gameData.perpetualTriggeredAbilityGrants.compute(cardId, (ignored, existing) -> {
            Map<EffectSlot, List<CardEffect>> updated = new EnumMap<>(EffectSlot.class);
            if (existing != null) {
                existing.forEach((slot, effects) -> updated.put(slot, new ArrayList<>(effects)));
            }
            List<CardEffect> effects = updated.computeIfAbsent(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE,
                    ignoredSlot -> new ArrayList<>());
            if (!effects.contains(grantedAbility)) {
                effects.add(grantedAbility);
            }
            updated.replaceAll((slot, slotEffects) -> List.copyOf(slotEffects));
            return Map.copyOf(updated);
        });

        if (!target.getPersistentTriggeredEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE)
                .contains(grantedAbility)) {
            target.addPersistentTriggeredEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE, grantedAbility);
        }
    }
}
