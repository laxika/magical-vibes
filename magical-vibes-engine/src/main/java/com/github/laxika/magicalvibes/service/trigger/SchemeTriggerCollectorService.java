package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AbandonSchemeAndSetTriggeringSchemeInMotionAgainEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SchemeSetInMotionTriggerEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/** Collects triggers caused by a scheme being set in motion. */
@Component
@RequiredArgsConstructor
public class SchemeTriggerCollectorService {

    private final PredicateEvaluationService predicateEvaluationService;

    @CollectsTrigger(value = SchemeSetInMotionTriggerEffect.class,
            slot = EffectSlot.ON_CONTROLLER_SETS_SCHEME_IN_MOTION)
    private boolean handleSchemeSetInMotion(TriggerMatchContext match,
                                            SchemeSetInMotionTriggerEffect trigger,
                                            TriggerContext context) {
        TriggerContext.SchemeSetInMotion schemeSetInMotion = (TriggerContext.SchemeSetInMotion) context;
        StackEntry schemeEntry = schemeSetInMotion.schemeEntry();
        Card schemeCard = schemeEntry == null ? null : schemeEntry.getCard();
        if (schemeCard == null
                || !schemeSetInMotion.settingPlayerId().equals(match.controllerId())
                || (trigger.schemeFilter() != null
                && !predicateEvaluationService.matchesCardPredicate(
                schemeCard, trigger.schemeFilter(), null, match.gameData(), match.controllerId()))) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(trigger.resolvedEffects()),
                null,
                match.permanent().getId());
        entry.replaceEffectsToResolve(entry.getEffectsToResolve().stream()
                .map(effect -> bindSchemeSnapshot(effect, schemeEntry))
                .toList());
        entry.setTriggeringCardId(schemeCard.getId());
        entry.setNonTargeting(true);
        match.gameData().enqueueTrigger(entry);
        return true;
    }

    private CardEffect bindSchemeSnapshot(CardEffect effect, StackEntry schemeEntry) {
        if (effect instanceof AbandonSchemeAndSetTriggeringSchemeInMotionAgainEffect repeat
                && repeat.schemeSnapshot() == null) {
            return new AbandonSchemeAndSetTriggeringSchemeInMotionAgainEffect(new StackEntry(schemeEntry));
        }
        if (effect instanceof MayEffect may) {
            CardEffect wrapped = bindSchemeSnapshot(may.wrapped(), schemeEntry);
            CardEffect elseEffect = may.elseEffect() == null
                    ? null : bindSchemeSnapshot(may.elseEffect(), schemeEntry);
            return new MayEffect(wrapped, may.prompt(), elseEffect, may.choicePlayer());
        }
        return effect;
    }
}
