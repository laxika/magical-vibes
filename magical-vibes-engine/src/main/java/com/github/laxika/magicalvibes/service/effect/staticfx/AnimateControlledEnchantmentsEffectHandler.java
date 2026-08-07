package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AnimateControlledEnchantmentsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Applies {@link AnimateControlledEnchantmentsEffect} (Starfield of Nyx). The mana-value base P/T
 * itself is contributed by the layered pass in sublayer 7b; this handler only marks the affected
 * permanent as animated for the assembled {@code StaticBonus}.
 */
@Component
@RequiredArgsConstructor
public class AnimateControlledEnchantmentsEffectHandler implements StaticEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnimateControlledEnchantmentsEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        AnimateControlledEnchantmentsEffect animate = (AnimateControlledEnchantmentsEffect) effect;
        Permanent target = context.target();
        if (!context.targetOnSameBattlefield()) return;
        if (context.source() != null && context.source().getId().equals(target.getId())) return;
        if (!gameQueryService.isEnchantment(target)
                || target.getCard().getSubtypes().contains(CardSubtype.AURA)) {
            return;
        }
        if (countEnchantments(context) < animate.minEnchantments()) return;
        accumulator.setAnimatedCreature(true);
    }

    private int countEnchantments(StaticEffectContext context) {
        List<Permanent> battlefield =
                context.gameData().playerBattlefields.get(context.sourceControllerId());
        if (battlefield == null) return 0;
        return (int) battlefield.stream().filter(gameQueryService::isEnchantment).count();
    }
}
