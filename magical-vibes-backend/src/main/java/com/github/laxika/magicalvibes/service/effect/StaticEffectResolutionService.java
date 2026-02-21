package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesBySubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedCreaturePerControlledSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostByOtherCreaturesWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.BoostNonColorCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostOtherCreaturesByColorEffect;
import com.github.laxika.magicalvibes.model.effect.BoostOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityToOwnLandsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostBySharedCreatureTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect.Scope;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledCreatureCountEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledLandCountEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledSubtypeCountEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToCreatureCardsInAllGraveyardsEffect;
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
        registry.register(BoostEnchantedCreaturePerControlledSubtypeEffect.class, this::resolveBoostEnchantedCreaturePerControlledSubtype);
        registry.register(BoostEquippedCreatureEffect.class, this::resolveBoostEquippedCreature);
        registry.register(GrantKeywordEffect.class, this::resolveGrantKeyword);
        registry.register(BoostOwnCreaturesEffect.class, this::resolveBoostOwnCreatures);
        registry.register(BoostOtherCreaturesByColorEffect.class, this::resolveBoostOtherCreaturesByColor);
        registry.register(BoostNonColorCreaturesEffect.class, this::resolveBoostNonColorCreatures);
        registry.register(GrantActivatedAbilityToEnchantedCreatureEffect.class, this::resolveGrantActivatedAbilityToEnchantedCreature);
        registry.register(GrantActivatedAbilityToOwnLandsEffect.class, this::resolveGrantActivatedAbilityToOwnLands);
        registry.register(BoostBySharedCreatureTypeEffect.class, this::resolveBoostBySharedCreatureType);

        registry.registerSelfHandler(BoostByOtherCreaturesWithSameNameEffect.class, this::resolveBoostByOtherCreaturesWithSameName);
        registry.registerSelfHandler(PowerToughnessEqualToControlledCreatureCountEffect.class, this::resolvePowerToughnessEqualToControlledCreatureCount);
        registry.registerSelfHandler(PowerToughnessEqualToControlledLandCountEffect.class, this::resolvePowerToughnessEqualToControlledLandCount);
        registry.registerSelfHandler(PowerToughnessEqualToControlledSubtypeCountEffect.class, this::resolvePowerToughnessEqualToControlledSubtypeCount);
        registry.registerSelfHandler(PowerToughnessEqualToCreatureCardsInAllGraveyardsEffect.class, this::resolvePowerToughnessEqualToCreatureCardsInAllGraveyards);
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

    private void resolveBoostEnchantedCreaturePerControlledSubtype(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostEnchantedCreaturePerControlledSubtypeEffect) effect;
        if (context.source().getAttachedTo() == null
                || !context.source().getAttachedTo().equals(context.target().getId())) {
            return;
        }

        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) {
            return;
        }

        List<Permanent> battlefield = context.gameData().playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        int count = 0;
        for (Permanent permanent : battlefield) {
            if (permanent.getCard().getSubtypes().contains(boost.subtype())) {
                count++;
            }
        }

        accumulator.addPower(count * boost.powerPerSubtype());
        accumulator.addToughness(count * boost.toughnessPerSubtype());
    }

    private void resolveBoostEquippedCreature(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostEquippedCreatureEffect) effect;
        if (context.source().getAttachedTo() != null
                && context.source().getAttachedTo().equals(context.target().getId())) {
            accumulator.addPower(boost.powerBoost());
            accumulator.addToughness(boost.toughnessBoost());
        }
    }

    private void resolveGrantKeyword(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantKeywordEffect) effect;
        if (grant.scope() == Scope.ENCHANTED_CREATURE || grant.scope() == Scope.EQUIPPED_CREATURE) {
            if (context.source().getAttachedTo() != null
                    && context.source().getAttachedTo().equals(context.target().getId())) {
                accumulator.addKeyword(grant.keyword());
            }
            return;
        }
        if (grant.scope() == Scope.OWN_TAPPED_CREATURES && context.targetOnSameBattlefield() && context.target().isTapped()) {
            accumulator.addKeyword(grant.keyword());
            return;
        }
        if (grant.scope() == Scope.OWN_CREATURES && context.targetOnSameBattlefield()) {
            boolean hasAnimateArtifacts = hasAnimateArtifactEffect(context.gameData());
            if (isEffectivelyCreature(context.target(), hasAnimateArtifacts)) {
                accumulator.addKeyword(grant.keyword());
            }
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

    private void resolveGrantActivatedAbilityToEnchantedCreature(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantActivatedAbilityToEnchantedCreatureEffect) effect;
        if (context.source().getAttachedTo() != null
                && context.source().getAttachedTo().equals(context.target().getId())) {
            accumulator.addActivatedAbility(grant.ability());
        }
    }

    private void resolveGrantActivatedAbilityToOwnLands(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantActivatedAbilityToOwnLandsEffect) effect;
        if (!context.targetOnSameBattlefield()) {
            return;
        }
        if (context.target().getCard().getType() == CardType.LAND
                || context.target().getCard().getAdditionalTypes().contains(CardType.LAND)) {
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

    private void resolvePowerToughnessEqualToCreatureCardsInAllGraveyards(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        GameData gameData = context.gameData();
        int count = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (card.getType() == CardType.CREATURE || card.getAdditionalTypes().contains(CardType.CREATURE)) {
                    count++;
                }
            }
        }
        accumulator.addPower(count);
        accumulator.addToughness(count);
    }

    private void resolveBoostByOtherCreaturesWithSameName(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostByOtherCreaturesWithSameNameEffect) effect;
        String sourceName = context.source().getCard().getName();
        GameData gameData = context.gameData();
        boolean hasAnimateArtifacts = hasAnimateArtifactEffect(gameData);

        int count = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (permanent.getId().equals(context.source().getId())) continue;
                if (!isEffectivelyCreature(permanent, hasAnimateArtifacts)) continue;
                if (!sourceName.equals(permanent.getCard().getName())) continue;
                count++;
            }
        }

        accumulator.addPower(count * boost.powerPerCreature());
        accumulator.addToughness(count * boost.toughnessPerCreature());
    }

    private void resolvePowerToughnessEqualToControlledSubtypeCount(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var pt = (PowerToughnessEqualToControlledSubtypeCountEffect) effect;
        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) {
            return;
        }

        List<Permanent> battlefield = context.gameData().playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        int count = 0;
        for (Permanent permanent : battlefield) {
            if (permanent.getCard().getSubtypes().contains(pt.subtype())) {
                count++;
            }
        }

        accumulator.addPower(count);
        accumulator.addToughness(count);
    }

    private void resolvePowerToughnessEqualToControlledLandCount(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) {
            return;
        }

        List<Permanent> battlefield = context.gameData().playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        int count = 0;
        for (Permanent permanent : battlefield) {
            if (permanent.getCard().getType() == CardType.LAND
                    || permanent.getCard().getAdditionalTypes().contains(CardType.LAND)) {
                count++;
            }
        }

        accumulator.addPower(count);
        accumulator.addToughness(count);
    }

    private void resolvePowerToughnessEqualToControlledCreatureCount(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) {
            return;
        }

        List<Permanent> battlefield = context.gameData().playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        boolean hasAnimateArtifacts = hasAnimateArtifactEffect(context.gameData());
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (isEffectivelyCreature(permanent, hasAnimateArtifacts)) {
                count++;
            }
        }

        accumulator.addPower(count);
        accumulator.addToughness(count);
    }

    private UUID findControllerId(GameData gameData, Permanent permanent) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.contains(permanent)) {
                return playerId;
            }
        }
        return null;
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

