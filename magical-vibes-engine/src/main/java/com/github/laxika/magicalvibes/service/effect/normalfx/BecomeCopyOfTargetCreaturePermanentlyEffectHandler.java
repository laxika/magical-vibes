package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreaturePermanentlyEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeCopyOfTargetCreaturePermanentlyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCopierService permanentCopierService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfTargetCreaturePermanentlyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        BecomeCopyOfTargetCreaturePermanentlyEffect copyEffect =
                (BecomeCopyOfTargetCreaturePermanentlyEffect) effect;
        List<java.util.UUID> boundTargets = entry.targetsForBoundEffectGroup(copyEffect);
        java.util.UUID targetId = boundTargets != null
                ? boundTargets.isEmpty() ? null : boundTargets.getFirst()
                : entry.getTargetId() != null ? entry.getTargetId()
                : entry.getTargetIds().isEmpty() ? null : entry.getTargetIds().getFirst();
        if (entry.getSourcePermanentId() == null || targetId == null) return;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (source == null || target == null) {
            log.info("Game {} - Creature-copy source or target no longer on the battlefield", gameData.id);
            return;
        }

        Card originalCard = source.getCard();
        EffectRegistration retainedRegistration = findRetainedRegistration(originalCard, copyEffect);
        var additionalRetainedRegistrations = copyEffect.additionalRetainedEffectSlots().stream()
                .flatMap(slot -> originalCard.getEffectRegistrations(slot).stream()
                        .map(registration -> new RetainedRegistration(slot, registration)))
                .toList();
        List<CardEffect> retainedTargetingEffects = new ArrayList<>();
        if (retainedRegistration != null) {
            retainedTargetingEffects.add(retainedRegistration.effect());
        }
        additionalRetainedRegistrations.stream()
                .map(retained -> retained.registration().effect())
                .forEach(retainedTargetingEffects::add);
        String originalName = source.getCard().getName();
        permanentCopierService.applyCloneCopy(source, target, null, null);
        if (copyEffect.nameOverride() != null) {
            source.getCard().setName(copyEffect.nameOverride());
        }
        if (retainedRegistration != null) {
            EffectSlot slot = copyEffect.retainedEffectSlot();
            source.getCard().addEffect(slot, retainedRegistration.effect(), retainedRegistration.triggerMode());
        }
        for (RetainedRegistration retained : additionalRetainedRegistrations) {
            source.getCard().addEffect(retained.slot(), retained.registration().effect(),
                    retained.registration().triggerMode());
        }
        source.getCard().appendSpellTargetingForEffectsFrom(originalCard, retainedTargetingEffects);
        log.info("Game {} - {} becomes a copy of {}", gameData.id, originalName, target.getCard().getName());
    }

    private EffectRegistration findRetainedRegistration(
            Card sourceCard, BecomeCopyOfTargetCreaturePermanentlyEffect copyEffect) {
        if (copyEffect.retainedEffectSlot() == null) {
            return null;
        }
        return sourceCard.getEffectRegistrations(copyEffect.retainedEffectSlot()).stream()
                .filter(registration -> containsEffect(registration.effect(), copyEffect))
                .findFirst()
                .orElse(null);
    }

    private boolean containsEffect(CardEffect candidate, CardEffect target) {
        if (candidate == null) {
            return false;
        }
        if (candidate.equals(target)) {
            return true;
        }
        if (candidate instanceof SequenceEffect sequence) {
            return sequence.steps().stream().anyMatch(step -> containsEffect(step, target));
        }
        if (candidate instanceof ConditionalEffect conditional) {
            return containsEffect(conditional.wrapped(), target);
        }
        if (candidate instanceof ConditionalReplacementEffect conditional) {
            return containsEffect(conditional.baseEffect(), target)
                    || containsEffect(conditional.upgradedEffect(), target);
        }
        if (candidate instanceof MayEffect may) {
            return containsEffect(may.wrapped(), target)
                    || containsEffect(may.elseEffect(), target);
        }
        return false;
    }

    private record RetainedRegistration(EffectSlot slot, EffectRegistration registration) {
    }
}
