package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesBySubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostNonColorCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostOtherCreaturesByColorEffect;
import com.github.laxika.magicalvibes.model.effect.BoostOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostBySharedCreatureTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToOwnTappedCreaturesEffect;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class StaticEffectResolutionService implements StaticEffectHandlerProvider {

    @Override
    public void registerHandlers(StaticEffectHandlerRegistry registry) {
        registry.register(AnimateNoncreatureArtifactsEffect.class, this::resolveAnimateNoncreatureArtifacts);
        registry.register(BoostCreaturesBySubtypeEffect.class, this::resolveBoostCreaturesBySubtype);
        registry.register(BoostEnchantedCreatureEffect.class, this::resolveBoostEnchantedCreature);
        registry.register(BoostEquippedCreatureEffect.class, this::resolveBoostEquippedCreature);
        registry.register(GrantKeywordToEnchantedCreatureEffect.class, this::resolveGrantKeywordToEnchantedCreature);
        registry.register(BoostOwnCreaturesEffect.class, this::resolveBoostOwnCreatures);
        registry.register(BoostOtherCreaturesByColorEffect.class, this::resolveBoostOtherCreaturesByColor);
        registry.register(BoostNonColorCreaturesEffect.class, this::resolveBoostNonColorCreatures);
        registry.register(GrantKeywordToOwnTappedCreaturesEffect.class, this::resolveGrantKeywordToOwnTappedCreatures);
        registry.register(GrantActivatedAbilityToEnchantedCreatureEffect.class, this::resolveGrantActivatedAbilityToEnchantedCreature);
        registry.register(BoostBySharedCreatureTypeEffect.class, this::resolveBoostBySharedCreatureType);
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

    private void resolveBoostEquippedCreature(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostEquippedCreatureEffect) effect;
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

    private void resolveGrantActivatedAbilityToEnchantedCreature(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantActivatedAbilityToEnchantedCreatureEffect) effect;
        if (context.source().getAttachedTo() != null
                && context.source().getAttachedTo().equals(context.target().getId())) {
            accumulator.addActivatedAbility(grant.ability());
        }
    }

    private void resolveBoostBySharedCreatureType(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        Permanent target = context.target();
        GameData gameData = context.gameData();

        List<CardSubtype> targetTypes = new ArrayList<>(target.getCard().getSubtypes());
        targetTypes.addAll(target.getGrantedSubtypes());
        boolean targetIsChangeling = target.hasKeyword(Keyword.CHANGELING);

        if (targetTypes.isEmpty() && !targetIsChangeling) return;

        boolean hasAnimateArtifacts = hasAnimateArtifactEffect(gameData);
        int count = 0;

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent other : bf) {
                if (other == target) continue;
                if (!isEffectivelyCreature(other, hasAnimateArtifacts)) continue;

                List<CardSubtype> otherTypes = new ArrayList<>(other.getCard().getSubtypes());
                otherTypes.addAll(other.getGrantedSubtypes());
                boolean otherIsChangeling = other.hasKeyword(Keyword.CHANGELING);

                if (otherTypes.isEmpty() && !otherIsChangeling) continue;

                boolean sharesType = (targetIsChangeling && (otherIsChangeling || !otherTypes.isEmpty()))
                        || (otherIsChangeling && !targetTypes.isEmpty())
                        || targetTypes.stream().anyMatch(otherTypes::contains);

                if (sharesType) count++;
            }
        }

        accumulator.addPower(count);
        accumulator.addToughness(count);
    }

    private boolean isEffectivelyCreature(Permanent permanent, boolean hasAnimateArtifacts) {
        if (permanent.getCard().getType() == CardType.CREATURE) return true;
        if (permanent.getCard().getAdditionalTypes().contains(CardType.CREATURE)) return true;
        if (permanent.isAnimatedUntilEndOfTurn()) return true;
        return hasAnimateArtifacts && (permanent.getCard().getType() == CardType.ARTIFACT
                || permanent.getCard().getAdditionalTypes().contains(CardType.ARTIFACT));
    }

    private boolean hasAnimateArtifactEffect(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent source : bf) {
                for (CardEffect e : source.getCard().getEffects(EffectSlot.STATIC)) {
                    if (e instanceof AnimateNoncreatureArtifactsEffect) return true;
                }
            }
        }
        return false;
    }
}
