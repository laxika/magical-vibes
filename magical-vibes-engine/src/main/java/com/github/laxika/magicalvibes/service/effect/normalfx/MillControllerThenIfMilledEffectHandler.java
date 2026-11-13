package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerThenIfMilledEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MillControllerThenIfMilledEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final TriggeredAbilityStackService triggeredAbilityStackService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerThenIfMilledEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MillControllerThenIfMilledEffect) effect;
        UUID controllerId = entry.getControllerId();

        // A milled card that a replacement effect exiled (e.g. Rest in Peace) was still milled, and
        // an effect can refer to it in the public zone it moved to (CR 701.17c).
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int count = Math.max(0, amountEvaluationService.evaluate(gameData, e.count(),
                AmountContext.forStackEntry(entry, source)));
        List<Card> milled = graveyardService.resolveMillPlayerIncludingExiled(gameData, controllerId, count);
        int matchCount = (int) milled.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(card, e.filter(), null))
                .count();
        entry.setEventValue(matchCount);
        boolean matched = e.requireAllCardsMilled() ? milled.size() >= count : matchCount > 0;

        log.info("Game {} - {} milled {} card(s), condition {}",
                gameData.id, entry.getCard().getName(), milled.size(), matched ? "met" : "not met");

        CardEffect followUp = matched ? e.thenEffect() : e.elseEffect();
        if (followUp != null) {
            if (matched && e.thenEffectTargets()) {
                beginReflexiveTargetChoice(gameData, entry, followUp);
                return;
            }
            dispatch(gameData, entry, followUp);
        }
    }

    private void beginReflexiveTargetChoice(GameData gameData, StackEntry entry, CardEffect effect) {
        // "When you do" is an ability of its own (CR 603.12): its target is chosen as it goes on the stack, and it
        // records what the resolving entry was about.
        triggeredAbilityStackService.putMade(gameData, ReflexiveAbilitySupport.record(gameData, entry, List.of(effect)));
    }

    private void dispatch(GameData gameData, StackEntry entry, CardEffect effect) {
        if (effect instanceof SequenceEffect sequence) {
            for (CardEffect step : sequence.steps()) {
                dispatch(gameData, entry, step);
            }
            return;
        }

        EffectHandler handler = effectHandlerRegistry.getHandler(effect);
        if (handler != null) {
            handler.resolve(gameData, entry, effect);
        } else {
            log.warn("No handler for follow-up effect in MillControllerThenIfMilledEffect: {}",
                    effect.getClass().getSimpleName());
        }
    }
}
