package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetArtifactsThenConjurePerpetualCopiesIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves Flames of Moradin's artifact destruction and modified nontoken duplicates. */
@Component
@RequiredArgsConstructor
public class DestroyTargetArtifactsThenConjurePerpetualCopiesIntoHandEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetArtifactsThenConjurePerpetualCopiesIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        List<Permanent> targets = new ArrayList<>();
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target != null && gameQueryService.isArtifact(gameData, target)) {
                targets.add(target);
            }
        }

        List<Permanent> destroyed = destructionSupport.destroyBatchCollecting(
                gameData, targets, entry.getCard().getName(), false);
        for (Permanent destroyedPermanent : destroyed) {
            if (destroyedPermanent.getCard().isToken()) {
                continue;
            }

            Card copy = destroyedPermanent.getCard().createCardCopy();
            copy.setOwnerId(entry.getControllerId());
            copy.addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{R}"))));
            copy.freeze();
            addPerpetualEndStepSacrifice(gameData, copy.getId());
            gameData.addCardToHand(entry.getControllerId(), copy);
            gameLogService.append(gameData, GameLog.cardThen(copy, " is conjured into "
                    + gameData.playerIdToName.get(entry.getControllerId()) + "'s hand."));
        }
    }

    private void addPerpetualEndStepSacrifice(GameData gameData, UUID cardId) {
        gameData.perpetualTriggeredAbilityGrants.compute(cardId, (ignored, existing) -> {
            Map<EffectSlot, List<CardEffect>> updated = new EnumMap<>(EffectSlot.class);
            if (existing != null) {
                existing.forEach((slot, effects) -> updated.put(slot, new ArrayList<>(effects)));
            }
            updated.computeIfAbsent(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                    ignoredSlot -> new ArrayList<>()).add(new SacrificeSelfEffect());
            updated.replaceAll((slot, effects) -> List.copyOf(effects));
            return Map.copyOf(updated);
        });
    }
}
