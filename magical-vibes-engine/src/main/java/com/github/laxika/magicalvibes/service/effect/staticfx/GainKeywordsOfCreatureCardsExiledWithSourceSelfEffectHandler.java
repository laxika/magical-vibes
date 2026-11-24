package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCreatureCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.ProtectionGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService.LocatedCard;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Gives the source the watched keywords of the cards a {@link GainKeywordsOfCreatureCardsExiledWithSourceEffect}
 * names, when its recipient is the source; {@link GainKeywordsOfCreatureCardsExiledWithSourceEffectHandler} gives
 * them to another recipient.
 */
@Component
@RequiredArgsConstructor
public class GainKeywordsOfCreatureCardsExiledWithSourceSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainKeywordsOfCreatureCardsExiledWithSourceEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var keywordEffect = (GainKeywordsOfCreatureCardsExiledWithSourceEffect) effect;
        if (support.names(context, keywordEffect.recipient(), null)) {
            lend(context, keywordEffect, accumulator, gameQueryService, predicateEvaluationService);
        }
    }

    /**
     * Adds to {@code accumulator} each watched keyword of the cards the effect names, with its variants: a
     * "hexproof from [quality]" when hexproof is watched (CR 702.11d), and the cards' own protection abilities when
     * protection is.
     */
    static void lend(StaticEffectContext context, GainKeywordsOfCreatureCardsExiledWithSourceEffect keywordEffect,
                     StaticBonusAccumulator accumulator, GameQueryService gameQueryService,
                     PredicateEvaluationService predicateEvaluationService) {
        for (LocatedCard located : GainActivatedAbilitiesOfExiledCardsSelfEffectHandler.lendingCards(
                context.gameData(), gameQueryService, predicateEvaluationService, context.source(),
                context.sourceControllerId(), keywordEffect.cards())) {
            if (keywordEffect.cardFilter() != null && !predicateEvaluationService.matchesCardPredicate(located.card(),
                    keywordEffect.cardFilter(), context.source().getCard().getId(), context.gameData(),
                    located.ownerId())) {
                continue;
            }
            for (Keyword keyword : located.card().getKeywords()) {
                if (keywordEffect.watchedKeywords().contains(keyword)) {
                    accumulator.addKeyword(keyword);
                }
            }
            for (CardEffect staticEffect : located.card().getStaticEffects()) {
                if (keywordEffect.copiesProtection() && staticEffect instanceof ProtectionGrantingEffect protection
                        && (protection.protectionSubject() == null
                        || protection.protectionSubject() instanceof PermanentSubject.Source)) {
                    accumulator.addGrantedEffect(staticEffect);
                } else if (staticEffect instanceof TargetingRestrictionEffect restriction
                        && restriction.hexproofLike()
                        && keywordEffect.watchedKeywords().contains(Keyword.HEXPROOF)) {
                    accumulator.addGrantedEffect(staticEffect);
                }
            }
        }
    }
}
