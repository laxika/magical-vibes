package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesBySubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostNonColorCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostOtherCreaturesByColorEffect;
import com.github.laxika.magicalvibes.model.effect.BoostOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToOwnTappedCreaturesEffect;
import org.springframework.stereotype.Service;

@Service
public class StaticEffectResolutionService implements StaticEffectHandlerProvider {

    @Override
    public void registerHandlers(StaticEffectHandlerRegistry registry) {
        registry.register(AnimateNoncreatureArtifactsEffect.class, this::resolveAnimateNoncreatureArtifacts);
        registry.register(BoostCreaturesBySubtypeEffect.class, this::resolveBoostCreaturesBySubtype);
        registry.register(BoostEnchantedCreatureEffect.class, this::resolveBoostEnchantedCreature);
        registry.register(GrantKeywordToEnchantedCreatureEffect.class, this::resolveGrantKeywordToEnchantedCreature);
        registry.register(BoostOwnCreaturesEffect.class, this::resolveBoostOwnCreatures);
        registry.register(BoostOtherCreaturesByColorEffect.class, this::resolveBoostOtherCreaturesByColor);
        registry.register(BoostNonColorCreaturesEffect.class, this::resolveBoostNonColorCreatures);
        registry.register(GrantKeywordToOwnTappedCreaturesEffect.class, this::resolveGrantKeywordToOwnTappedCreatures);
    }

    private void resolveAnimateNoncreatureArtifacts(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        if (context.target().getCard().getType() == CardType.ARTIFACT) {
            accumulator.setAnimatedCreature(true);
        }
    }

    private void resolveBoostCreaturesBySubtype(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostCreaturesBySubtypeEffect) effect;
        if (context.target().hasKeyword(Keyword.CHANGELING)
                || context.target().getCard().getSubtypes().stream().anyMatch(boost.affectedSubtypes()::contains)) {
            accumulator.addPower(boost.powerBoost());
            accumulator.addToughness(boost.toughnessBoost());
            accumulator.addKeywords(boost.grantedKeywords());
        }
    }

    private void resolveBoostEnchantedCreature(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostEnchantedCreatureEffect) effect;
        if (context.source().getAttachedTo() != null
                && context.source().getAttachedTo().equals(context.target().getId())) {
            accumulator.addPower(boost.powerBoost());
            accumulator.addToughness(boost.toughnessBoost());
        }
    }

    private void resolveGrantKeywordToEnchantedCreature(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantKeywordToEnchantedCreatureEffect) effect;
        if (context.source().getAttachedTo() != null
                && context.source().getAttachedTo().equals(context.target().getId())) {
            accumulator.addKeyword(grant.keyword());
        }
    }

    private void resolveBoostOwnCreatures(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostOwnCreaturesEffect) effect;
        if (context.targetOnSameBattlefield()) {
            accumulator.addPower(boost.powerBoost());
            accumulator.addToughness(boost.toughnessBoost());
        }
    }

    private void resolveBoostOtherCreaturesByColor(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostOtherCreaturesByColorEffect) effect;
        if (context.target().getCard().getColor() == boost.color()) {
            accumulator.addPower(boost.powerBoost());
            accumulator.addToughness(boost.toughnessBoost());
        }
    }

    private void resolveBoostNonColorCreatures(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostNonColorCreaturesEffect) effect;
        if (context.target().getCard().getColor() != boost.excludedColor()) {
            accumulator.addPower(boost.powerBoost());
            accumulator.addToughness(boost.toughnessBoost());
        }
    }

    private void resolveGrantKeywordToOwnTappedCreatures(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantKeywordToOwnTappedCreaturesEffect) effect;
        if (context.targetOnSameBattlefield() && context.target().isTapped()) {
            accumulator.addKeyword(grant.keyword());
        }
    }
}
