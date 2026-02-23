package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedCreaturePerControlledSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.BoostByOtherCreaturesWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerEnchantmentOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityToOwnLandsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostBySharedCreatureTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MetalcraftKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledCreatureCountEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledLandCountEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledSubtypeCountEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToCreatureCardsInAllGraveyardsEffect;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

@Service
public class StaticEffectResolutionService {

    @HandlesStaticEffect(AnimateNoncreatureArtifactsEffect.class)
    private void resolveAnimateNoncreatureArtifacts(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        if (context.target().getCard().getType() == CardType.ARTIFACT) {
            accumulator.setAnimatedCreature(true);
        }
    }

    @HandlesStaticEffect(BoostAttachedCreatureEffect.class)
    private void resolveBoostAttachedCreature(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostAttachedCreatureEffect) effect;
        if (context.source().getAttachedTo() != null
                && context.source().getAttachedTo().equals(context.target().getId())) {
            accumulator.addPower(boost.powerBoost());
            accumulator.addToughness(boost.toughnessBoost());
        }
    }

    @HandlesStaticEffect(BoostEnchantedCreaturePerControlledSubtypeEffect.class)
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

    @HandlesStaticEffect(GrantKeywordEffect.class)
    private void resolveGrantKeyword(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantKeywordEffect) effect;
        if (grant.scope() == GrantScope.ENCHANTED_CREATURE || grant.scope() == GrantScope.EQUIPPED_CREATURE) {
            if (context.source().getAttachedTo() != null
                    && context.source().getAttachedTo().equals(context.target().getId())) {
                accumulator.addKeyword(grant.keyword());
            }
            return;
        }
        if (grant.scope() == GrantScope.OWN_TAPPED_CREATURES && context.targetOnSameBattlefield() && context.target().isTapped()) {
            accumulator.addKeyword(grant.keyword());
            return;
        }
        if (grant.scope() == GrantScope.OWN_CREATURES && context.targetOnSameBattlefield()) {
            boolean hasAnimateArtifacts = hasAnimateArtifactEffect(context.gameData());
            if (isEffectivelyCreature(context.target(), hasAnimateArtifacts)
                    && matchesStaticFilter(context.target(), grant.filter())) {
                accumulator.addKeyword(grant.keyword());
            }
        }
        if (grant.scope() == GrantScope.ALL_CREATURES) {
            boolean hasAnimateArtifacts = hasAnimateArtifactEffect(context.gameData());
            if (isEffectivelyCreature(context.target(), hasAnimateArtifacts)
                    && matchesStaticFilter(context.target(), grant.filter())) {
                accumulator.addKeyword(grant.keyword());
            }
        }
    }

    @HandlesStaticEffect(GrantEffectEffect.class)
    private void resolveGrantEffectEffect(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantEffectEffect) effect;
        if (grant.scope() == GrantScope.ENCHANTED_CREATURE || grant.scope() == GrantScope.EQUIPPED_CREATURE) {
            if (context.source().getAttachedTo() != null
                    && context.source().getAttachedTo().equals(context.target().getId())) {
                accumulator.addGrantedEffect(grant.effect());
            }
            return;
        }
        if (grant.scope() == GrantScope.OWN_TAPPED_CREATURES && context.targetOnSameBattlefield() && context.target().isTapped()) {
            accumulator.addGrantedEffect(grant.effect());
            return;
        }
        if (grant.scope() == GrantScope.OWN_CREATURES && context.targetOnSameBattlefield()) {
            boolean hasAnimateArtifacts = hasAnimateArtifactEffect(context.gameData());
            if (isEffectivelyCreature(context.target(), hasAnimateArtifacts)
                    && matchesStaticFilter(context.target(), grant.filter())) {
                accumulator.addGrantedEffect(grant.effect());
            }
        }
        if (grant.scope() == GrantScope.ALL_CREATURES) {
            boolean hasAnimateArtifacts = hasAnimateArtifactEffect(context.gameData());
            if (isEffectivelyCreature(context.target(), hasAnimateArtifacts)
                    && matchesStaticFilter(context.target(), grant.filter())) {
                accumulator.addGrantedEffect(grant.effect());
            }
        }
    }

    @HandlesStaticEffect(StaticBoostEffect.class)
    private void resolveStaticBoost(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (StaticBoostEffect) effect;
        boolean scopeMatch = switch (boost.scope()) {
            case OWN_CREATURES -> context.targetOnSameBattlefield();
            case ALL_CREATURES -> true;
            default -> false;
        };
        if (scopeMatch && matchesStaticFilter(context.target(), boost.filter())) {
            accumulator.addPower(boost.powerBoost());
            accumulator.addToughness(boost.toughnessBoost());
            accumulator.addKeywords(boost.grantedKeywords());
        }
    }

    @HandlesStaticEffect(GrantActivatedAbilityToEnchantedCreatureEffect.class)
    private void resolveGrantActivatedAbilityToEnchantedCreature(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantActivatedAbilityToEnchantedCreatureEffect) effect;
        if (context.source().getAttachedTo() != null
                && context.source().getAttachedTo().equals(context.target().getId())) {
            accumulator.addActivatedAbility(grant.ability());
        }
    }

    @HandlesStaticEffect(GrantActivatedAbilityToOwnLandsEffect.class)
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

    @HandlesStaticEffect(BoostBySharedCreatureTypeEffect.class)
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

    @HandlesStaticEffect(value = MetalcraftKeywordEffect.class, selfOnly = true)
    private void resolveMetalcraftKeyword(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var metalcraft = (MetalcraftKeywordEffect) effect;
        int artifactCount = countControlledPermanents(context,
                p -> p.getCard().getType() == CardType.ARTIFACT || p.getCard().getAdditionalTypes().contains(CardType.ARTIFACT));
        if (artifactCount >= 3) {
            accumulator.addKeyword(metalcraft.keyword());
            accumulator.addPower(metalcraft.powerBoost());
            accumulator.addToughness(metalcraft.toughnessBoost());
        }
    }

    private boolean isEffectivelyCreature(Permanent permanent, boolean hasAnimateArtifacts) {
        if (permanent.getCard().getType() == CardType.CREATURE) return true;
        if (permanent.getCard().getAdditionalTypes().contains(CardType.CREATURE)) return true;
        if (permanent.isAnimatedUntilEndOfTurn()) return true;
        return hasAnimateArtifacts && (permanent.getCard().getType() == CardType.ARTIFACT
                || permanent.getCard().getAdditionalTypes().contains(CardType.ARTIFACT));
    }

    @HandlesStaticEffect(value = PowerToughnessEqualToCreatureCardsInAllGraveyardsEffect.class, selfOnly = true)
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

    @HandlesStaticEffect(value = BoostByOtherCreaturesWithSameNameEffect.class, selfOnly = true)
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

    @HandlesStaticEffect(value = BoostSelfPerEnchantmentOnBattlefieldEffect.class, selfOnly = true)
    private void resolveBoostSelfPerEnchantmentOnBattlefield(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostSelfPerEnchantmentOnBattlefieldEffect) effect;
        int count = 0;
        for (UUID playerId : context.gameData().orderedPlayerIds) {
            List<Permanent> bf = context.gameData().playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent permanent : bf) {
                if (permanent.getCard().getType() == CardType.ENCHANTMENT
                        || permanent.getCard().getAdditionalTypes().contains(CardType.ENCHANTMENT)) {
                    count++;
                }
            }
        }
        accumulator.addPower(count * boost.powerPerEnchantment());
        accumulator.addToughness(count * boost.toughnessPerEnchantment());
    }

    @HandlesStaticEffect(value = PowerToughnessEqualToControlledSubtypeCountEffect.class, selfOnly = true)
    private void resolvePowerToughnessEqualToControlledSubtypeCount(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var pt = (PowerToughnessEqualToControlledSubtypeCountEffect) effect;
        int count = countControlledPermanents(context, p -> p.getCard().getSubtypes().contains(pt.subtype()));
        accumulator.addPower(count);
        accumulator.addToughness(count);
    }

    @HandlesStaticEffect(value = PowerToughnessEqualToControlledLandCountEffect.class, selfOnly = true)
    private void resolvePowerToughnessEqualToControlledLandCount(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        int count = countControlledPermanents(context,
                p -> p.getCard().getType() == CardType.LAND || p.getCard().getAdditionalTypes().contains(CardType.LAND));
        accumulator.addPower(count);
        accumulator.addToughness(count);
    }

    @HandlesStaticEffect(value = PowerToughnessEqualToControlledCreatureCountEffect.class, selfOnly = true)
    private void resolvePowerToughnessEqualToControlledCreatureCount(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        boolean hasAnimateArtifacts = hasAnimateArtifactEffect(context.gameData());
        int count = countControlledPermanents(context, p -> isEffectivelyCreature(p, hasAnimateArtifacts));
        accumulator.addPower(count);
        accumulator.addToughness(count);
    }

    private int countControlledPermanents(StaticEffectContext context, Predicate<Permanent> filter) {
        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) return 0;

        List<Permanent> battlefield = context.gameData().playerBattlefields.get(controllerId);
        if (battlefield == null) return 0;

        int count = 0;
        for (Permanent permanent : battlefield) {
            if (filter.test(permanent)) count++;
        }
        return count;
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

    private static final Set<CardSubtype> NON_CREATURE_SUBTYPES = EnumSet.of(
            CardSubtype.FOREST,
            CardSubtype.MOUNTAIN,
            CardSubtype.ISLAND,
            CardSubtype.PLAINS,
            CardSubtype.SWAMP,
            CardSubtype.AURA,
            CardSubtype.EQUIPMENT,
            CardSubtype.AJANI
    );

    static boolean matchesStaticFilter(Permanent target, PermanentPredicate filter) {
        if (filter == null) return true;
        if (filter instanceof PermanentColorInPredicate p)
            return p.colors().contains(target.getCard().getColor());
        if (filter instanceof PermanentHasSubtypePredicate p)
            return target.getCard().getSubtypes().contains(p.subtype())
                    || (isCreatureSubtype(p.subtype()) && target.hasKeyword(Keyword.CHANGELING));
        if (filter instanceof PermanentHasAnySubtypePredicate p)
            return target.getCard().getSubtypes().stream().anyMatch(p.subtypes()::contains)
                    || (p.subtypes().stream().anyMatch(StaticEffectResolutionService::isCreatureSubtype)
                    && target.hasKeyword(Keyword.CHANGELING));
        if (filter instanceof PermanentIsCreaturePredicate)
            return target.getCard().getType() == CardType.CREATURE
                    || target.getCard().getAdditionalTypes().contains(CardType.CREATURE);
        if (filter instanceof PermanentIsArtifactPredicate)
            return target.getCard().getType() == CardType.ARTIFACT
                    || target.getCard().getAdditionalTypes().contains(CardType.ARTIFACT);
        if (filter instanceof PermanentNotPredicate p)
            return !matchesStaticFilter(target, p.predicate());
        if (filter instanceof PermanentAllOfPredicate p)
            return p.predicates().stream().allMatch(inner -> matchesStaticFilter(target, inner));
        if (filter instanceof PermanentAnyOfPredicate p)
            return p.predicates().stream().anyMatch(inner -> matchesStaticFilter(target, inner));
        if (filter instanceof PermanentTruePredicate) return true;
        throw new IllegalArgumentException("Unsupported static filter predicate: " + filter.getClass().getSimpleName());
    }

    private static boolean isCreatureSubtype(CardSubtype subtype) {
        return !NON_CREATURE_SUBTYPES.contains(subtype);
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

