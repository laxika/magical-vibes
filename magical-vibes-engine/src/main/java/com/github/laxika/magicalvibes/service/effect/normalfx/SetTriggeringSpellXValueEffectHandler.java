package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SetTriggeringSpellXValueEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Sets the triggering spell's X to the evaluated amount. */
@Component
@RequiredArgsConstructor
public class SetTriggeringSpellXValueEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetTriggeringSpellXValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        StackEntry spell = gameQueryService.findStackEntryByCardId(gameData, entry.getTriggeringCardId());
        if (spell == null) {
            return;
        }
        spell.setXValue(amountEvaluationService.evaluate(gameData, ((SetTriggeringSpellXValueEffect) effect).amount(),
                AmountContext.forStackEntry(entry, entry.getSourcePermanentSnapshot())));
        spell.setXValueSetByCastTrigger(true);
    }
}
