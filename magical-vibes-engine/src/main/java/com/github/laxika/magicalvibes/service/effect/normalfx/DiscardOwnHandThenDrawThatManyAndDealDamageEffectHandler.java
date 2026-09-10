package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawThatManyAndDealDamageEffect;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves a hand discard/draw followed by conditional damage to each opponent. */
@Component
@RequiredArgsConstructor
public class DiscardOwnHandThenDrawThatManyAndDealDamageEffectHandler implements NormalEffectHandlerBean {

    private final DiscardOwnHandThenDrawThatManyEffectHandler discardAndDrawHandler;
    private final DealDamageToPlayersEffectHandler damageHandler;
    private final ConditionEvaluationService conditionEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardOwnHandThenDrawThatManyAndDealDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DiscardOwnHandThenDrawThatManyAndDealDamageEffect) effect;
        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        int discardedCount = hand == null ? 0 : hand.size();

        discardAndDrawHandler.resolve(gameData, entry, new DiscardOwnHandThenDrawThatManyEffect());
        entry.setEventValue(discardedCount);

        if (conditionEvaluationService.isMet(gameData, e.condition(),
                ConditionContext.forStackEntry(entry), entry.getEventValue())) {
            damageHandler.resolve(gameData, entry,
                    new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.EACH_OPPONENT));
        }
    }
}
