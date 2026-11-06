package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileIfHadCounterElseReturnWithCounterEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardFromGraveyardToBattlefieldEffect;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ExileIfHadCounterElseReturnWithCounterEffect} (Bogardan Phoenix): whether "it had a [counter] on
 * it" is read from the stack entry's triggering permanent as it last existed on the battlefield (CR 608.2h), and the
 * existing effect that exiles the card from the graveyard, or returns it with one such counter, resolves next on the
 * entry.
 */
@Component
public class ExileIfHadCounterElseReturnWithCounterEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileIfHadCounterElseReturnWithCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileIfHadCounterElseReturnWithCounterEffect) effect;
        CardEffect branch = entry.triggeringPermanentCounterCount(e.counterType()) >= 1
                ? new ExileSourceCardFromGraveyardEffect()
                : new ReturnTriggeringCardFromGraveyardToBattlefieldEffect(false, true, e.counterType(), 1);
        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(branch));
    }
}
