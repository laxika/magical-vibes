package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreaturePermanentlyEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
        if (entry.getSourcePermanentId() == null || entry.getTargetId() == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null || target == null) {
            log.info("Game {} - Creature-copy source or target no longer on the battlefield", gameData.id);
            return;
        }

        BecomeCopyOfTargetCreaturePermanentlyEffect copyEffect =
                (BecomeCopyOfTargetCreaturePermanentlyEffect) effect;
        EffectRegistration retainedRegistration = findRetainedRegistration(source.getCard(), copyEffect);
        String originalName = source.getCard().getName();
        permanentCopierService.applyCloneCopy(source, target, null, null);
        if (copyEffect.nameOverride() != null) {
            source.getCard().setName(copyEffect.nameOverride());
        }
        if (retainedRegistration != null) {
            EffectSlot slot = copyEffect.retainedEffectSlot();
            source.getCard().addEffect(slot, retainedRegistration.effect(), retainedRegistration.triggerMode());
        }
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
        if (candidate.equals(target)) {
            return true;
        }
        return candidate instanceof SequenceEffect sequence
                && sequence.steps().stream().anyMatch(step -> containsEffect(step, target));
    }
}
