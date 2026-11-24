package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PerpetuallyBoostCardEffect}; a change to the cards a subject names is logged as
 * {@link PerpetualChangeLog} says.
 */
@Component
@RequiredArgsConstructor
public class PerpetuallyBoostCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

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
        if (boost.cards() != null) {
            var located = subjectResolutionService.locatedCards(gameData, entry, boost, boost.cards());
            for (var card : located) {
                PerpetualCardPowerToughnessSupport.remember(gameData, card.card(), powerBoost, toughnessBoost);
            }
            PerpetualChangeLog.log(gameLogService, subjectResolutionService, gameData, entry, boost, boost.cards(),
                    located, String.format(" perpetually gets %+d/%+d.", powerBoost, toughnessBoost));
            return;
        }
        if (boost.card() == null) {
            // "That Vehicle perpetually gets …": the card of the permanent the trigger event was about, as it was then;
            // "it perpetually gets …" of a drawn card: the entry's triggering card.
            Permanent triggering = entry.getTriggeringPermanentSnapshot();
            Card triggeringCard = triggering != null ? triggering.getCard() : entry.triggeringSpellOrCard();
            if (triggeringCard != null) {
                PerpetualCardPowerToughnessSupport.remember(gameData, triggeringCard, powerBoost, toughnessBoost);
            }
            return;
        }
        // The copying permanent reads its physical card's modifier too (GameQueryService), so one record does.
        PerpetualCardPowerToughnessSupport.remember(gameData, boostedCard(boost, source), powerBoost, toughnessBoost);
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
        candidates.addAll(source.getCard().getTriggeredEffects());
        for (CardEffect candidate : candidates) {
            if (candidate == boost
                    || candidate instanceof PerpetuallyBoostCardEffect other && other.card() == boost.card()) {
                return source.getOriginalCard();
            }
        }
        return boost.card();
    }
}
