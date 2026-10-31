package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PerpetuallyBoostCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PerpetuallyBoostCardEffect boost = (PerpetuallyBoostCardEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        AmountContext context = AmountContext.forStackEntry(entry, source);
        int powerBoost = amountEvaluationService.evaluate(gameData, boost.powerBoost(), context);
        int toughnessBoost = amountEvaluationService.evaluate(gameData, boost.toughnessBoost(), context);
        Card boosted = boostedCard(boost, source);
        PerpetualCardPowerToughnessSupport.remember(gameData, boosted, powerBoost, toughnessBoost);
        if (boosted != boost.card() && source.getCard() != null
                && !source.getCard().getId().equals(boosted.getId())) {
            // The copying permanent reads its modifier under the card it currently shows.
            PerpetualCardPowerToughnessSupport.remember(gameData, source.getCard(), powerBoost, toughnessBoost);
        }

    }

    /**
     * The card the boost applies to. A card that boosts "itself" references its own printed instance,
     * so when the ability is being used by a copy of that card (Clone copying it) the physical card
     * of the copying permanent is boosted instead.
     */
    private static Card boostedCard(PerpetuallyBoostCardEffect boost, Permanent source) {
        if (source == null || source.getOriginalCard() == null || source.getOriginalCard() == boost.card()) {
            return boost.card();
        }
        java.util.List<CardEffect> candidates = new java.util.ArrayList<>(source.getCard().getCastingCostsAndSpellEffects());
        candidates.addAll(source.getCard().getStaticEffects());
        for (EffectSlot slot : EffectSlot.values()) {
            candidates.addAll(source.getCard().getEffects(slot));
        }
        for (CardEffect candidate : candidates) {
            if (candidate == boost
                    || candidate instanceof PerpetuallyBoostCardEffect other && other.card() == boost.card()) {
                return source.getOriginalCard();
            }
        }
        return boost.card();
    }
}
