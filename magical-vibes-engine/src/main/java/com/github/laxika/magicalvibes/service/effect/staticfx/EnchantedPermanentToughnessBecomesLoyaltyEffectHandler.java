package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentToughnessBecomesLoyaltyEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Applies the toughness the enchanted permanent has gained or spent through its toughness-as-loyalty
 * state as a modification on top of whatever base toughness other effects give it, so a later
 * base-power-and-toughness setter cannot undo loyalty costs or damage already paid in toughness.
 * Only the first Aura attached to the permanent contributes, so extra copies do not stack.
 */
@Component
public class EnchantedPermanentToughnessBecomesLoyaltyEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedPermanentToughnessBecomesLoyaltyEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        if (!context.source().isAttached()
                || !context.source().getAttachedTo().equals(context.target().getId())) {
            return;
        }
        Integer toughnessAsLoyalty = context.target().getToughnessAsLoyalty();
        if (toughnessAsLoyalty == null || !isFirstAttachedSource(context)) {
            return;
        }
        accumulator.addToughness(toughnessAsLoyalty - context.target().getBaseToughness());
    }

    private static boolean isFirstAttachedSource(StaticEffectContext context) {
        GameData gameData = context.gameData();
        if (gameData == null) {
            return true;
        }
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent candidate : battlefield) {
                if (candidate.isAttached()
                        && candidate.getAttachedTo().equals(context.target().getId())
                        && candidate.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(EnchantedPermanentToughnessBecomesLoyaltyEffect.class::isInstance)) {
                    return candidate.getId().equals(context.source().getId());
                }
            }
        }
        return true;
    }
}
