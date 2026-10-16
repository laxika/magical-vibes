package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardThenEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileSourceCardFromGraveyardThenEffectHandler implements NormalEffectHandlerBean {

    private final ExileSourceCardFromGraveyardEffectHandler exileHandler;
    private final GameQueryService gameQueryService;
    private final TriggeredAbilityQueueService triggeredAbilityQueueService;
    private final QueueReflexiveAbilityEffectHandler queueReflexiveAbilityEffectHandler;
    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSourceCardFromGraveyardThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileThen = (ExileSourceCardFromGraveyardThenEffect) effect;
        if (gameQueryService.findCardInGraveyardById(gameData, entry.getCard().getId()) == null) {
            return;
        }

        exileHandler.resolve(gameData, entry, new ExileSourceCardFromGraveyardEffect());

        if (gameQueryService.findCardInGraveyardById(gameData, entry.getCard().getId()) != null) {
            return;
        }

        if (!exileThen.reflexive()) {
            dispatch(gameData, entry, exileThen.thenEffect());
            return;
        }

        TargetSpec thenTargetSpec = exileThen.thenEffect().targetSpec();
        if (thenTargetSpec.equals(TargetSpec.NONE)) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    entry.getCard(),
                    entry.getControllerId(),
                    entry.getCard().getName() + "'s ability",
                    List.of(exileThen.thenEffect()),
                    null,
                    entry.getSourcePermanentId()));
        } else if (thenTargetSpec.admits(TargetPredicate.Kind.PERMANENT)
                || thenTargetSpec.admits(TargetPredicate.Kind.PLAYER)
                || thenTargetSpec.admits(TargetPredicate.Kind.SPELL)) {
            queueReflexiveAbilityEffectHandler.resolve(
                    gameData, entry, new QueueReflexiveAbilityEffect(exileThen.thenEffect()));
        } else {
            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    entry.getCard(), entry.getControllerId(), List.of(exileThen.thenEffect()),
                    null, 0, 0, 0, entry.getTriggeringPermanentPowerAtTrigger()));
            triggeredAbilityQueueService.processNextSpellGraveyardTargetTrigger(gameData);
        }
    }

    /**
     * Resolves the payload through its own handler against this entry. {@link SequenceEffect} has no
     * handler of its own, so a multi-step payload is expanded here and its steps must be synchronous.
     */
    private void dispatch(GameData gameData, StackEntry entry, CardEffect payload) {
        if (payload instanceof SequenceEffect sequence) {
            for (CardEffect step : sequence.steps()) {
                dispatch(gameData, entry, step);
            }
            return;
        }
        EffectHandler handler = effectHandlerRegistry.getHandler(payload);
        if (handler != null) {
            handler.resolve(gameData, entry, payload);
        }
    }
}
