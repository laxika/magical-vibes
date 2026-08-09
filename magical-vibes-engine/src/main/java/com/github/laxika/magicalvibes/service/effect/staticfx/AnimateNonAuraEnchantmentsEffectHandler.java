package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AnimateNonAuraEnchantmentsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Applies {@link AnimateNonAuraEnchantmentsEffect}. The layered pass supplies the type change and
 * the mana-value base P/T; this handler marks the affected permanent as animated for assembly.
 */
@Component
@RequiredArgsConstructor
public class AnimateNonAuraEnchantmentsEffectHandler implements StaticEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnimateNonAuraEnchantmentsEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        Permanent target = context.target();
        if (!gameQueryService.isEnchantment(target)
                || target.getCard().getSubtypes().contains(CardSubtype.AURA)) {
            return;
        }
        accumulator.setAnimatedCreature(true);
        accumulator.addGrantedCardType(CardType.CREATURE);
    }
}
