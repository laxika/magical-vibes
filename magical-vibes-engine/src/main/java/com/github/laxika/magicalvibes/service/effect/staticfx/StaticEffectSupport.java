package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Shared helpers used by every static effect handler. Per-effect {@link StaticEffectHandlerBean}
 * handlers reuse these helpers; behavior is identical to the original monolith privates.
 */
@Component
@RequiredArgsConstructor
public class StaticEffectSupport {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    private static final Set<CardSubtype> NON_CREATURE_SUBTYPES = EnumSet.of(
            CardSubtype.FOREST,
            CardSubtype.MOUNTAIN,
            CardSubtype.ISLAND,
            CardSubtype.PLAINS,
            CardSubtype.SWAMP,
            CardSubtype.AURA,
            CardSubtype.EQUIPMENT,
            CardSubtype.AJANI,
            CardSubtype.KOTH
    );

    /**
     * Returns true if the target matches the given creature-centric scope.
     * Handles ENCHANTED_CREATURE, ENCHANTED_PERMANENT, EQUIPPED_CREATURE, OWN_TAPPED_CREATURES, OWN_CREATURES, ALL_OWN_CREATURES, ALL_CREATURES.
     */
    public boolean matchesCreatureScope(StaticEffectContext context, GrantScope scope, PermanentPredicate filter) {
        if (scope == GrantScope.ENCHANTED_CREATURE || scope == GrantScope.ENCHANTED_PERMANENT || scope == GrantScope.EQUIPPED_CREATURE) {
            return context.source().isAttached()
                    && context.source().getAttachedTo().equals(context.target().getId())
                    && matchesStaticFilter(context.target(), filter);
        }
        if (scope == GrantScope.ENCHANTED_PLAYER_CREATURES) {
            if (!context.source().isAttached()) return false;
            UUID attachedPlayerId = context.source().getAttachedTo();
            List<Permanent> attachedPlayerBf = context.gameData().playerBattlefields.get(attachedPlayerId);
            if (attachedPlayerBf == null || !attachedPlayerBf.contains(context.target())) return false;
            boolean hasAnimateArtifacts = hasAnimateArtifactEffect(context.gameData());
            return isEffectivelyCreature(context.gameData(), context.target(), hasAnimateArtifacts)
                    && matchesStaticFilter(context.target(), filter);
        }
        if (scope == GrantScope.OWN_TAPPED_CREATURES) {
            return context.targetOnSameBattlefield() && context.target().isTapped();
        }
        if (scope == GrantScope.OWN_CREATURES || scope == GrantScope.ALL_OWN_CREATURES
                || scope == GrantScope.OPPONENT_CREATURES || scope == GrantScope.ALL_CREATURES) {
            boolean ownCheck = scope == GrantScope.ALL_CREATURES
                    || (scope == GrantScope.OWN_CREATURES && context.targetOnSameBattlefield())
                    || (scope == GrantScope.ALL_OWN_CREATURES && context.targetOnSameBattlefield())
                    || (scope == GrantScope.OPPONENT_CREATURES && !context.targetOnSameBattlefield());
            if (!ownCheck) return false;
            boolean hasAnimateArtifacts = hasAnimateArtifactEffect(context.gameData());
            return isEffectivelyCreature(context.gameData(), context.target(), hasAnimateArtifacts)
                    && matchesStaticFilter(context.target(), filter);
        }
        return false;
    }

    public boolean isEffectivelyCreature(Permanent permanent, boolean hasAnimateArtifacts) {
        return isEffectivelyCreature(null, permanent, hasAnimateArtifacts);
    }

    public boolean isEffectivelyCreature(GameData gameData, Permanent permanent, boolean hasAnimateArtifacts) {
        if (permanent.getCard().hasType(CardType.CREATURE)) return true;
        if (permanent.isAnimatedUntilEndOfTurn()) return true;
        if (permanent.isAnimatedUntilNextTurn()) return true;
        if (permanent.getCounterCount(CounterType.AWAKENING) > 0) return true;
        if (hasAnimateArtifacts && gameQueryService.isArtifact(permanent)) return true;
        if (gameData != null) return gameQueryService.hasSelfBecomeCreatureEffect(gameData, permanent);
        return false;
    }

    public void applySelfOnlyConditionalStaticEffect(StaticEffectContext context, CardEffect wrapped, StaticBonusAccumulator accumulator) {
        if (wrapped instanceof StaticBoostEffect boost) {
            if (selfInScope(context, boost.scope(), boost.filter())) {
                accumulator.addPower(boost.powerBoost());
                accumulator.addToughness(boost.toughnessBoost());
                accumulator.addKeywords(boost.grantedKeywords());
            }
        } else if (wrapped instanceof GrantKeywordEffect grant) {
            if (selfInScope(context, grant.scope(), grant.filter())) {
                accumulator.addKeywords(grant.keywords());
            }
        } else if (wrapped instanceof ProtectionFromColorsEffect protection) {
            accumulator.addProtectionColors(protection.colors());
        } else if (wrapped instanceof GrantEffectEffect grant) {
            if (grant.scope() == GrantScope.SELF || matchesStaticFilter(context.target(), grant.filter())) {
                accumulator.addGrantedEffect(grant.effect());
            }
        } else if (wrapped instanceof AnimatePermanentsEffect animate && animate.scope() == GrantScope.SELF) {
            accumulator.setSelfBecomeCreature(true);
            // Conditional self-become-creature statics (Rusted Relic, Warden of the Wall) always use
            // a fixed base P/T; static bonus computation has no stack entry to evaluate a dynamic amount.
            accumulator.addPower(animate.power() instanceof Fixed p ? p.value() : 0);
            accumulator.addToughness(animate.toughness() instanceof Fixed t ? t.value() : 0);
            for (CardSubtype subtype : animate.grantedSubtypes()) {
                accumulator.addGrantedSubtype(subtype);
            }
            accumulator.addKeywords(animate.grantedKeywords());
        }
    }

    /**
     * Whether a conditional static effect's scope covers the source permanent itself.
     * {@link GrantScope#OWN_CREATURES} means "other creatures you control" and is excluded;
     * attachment scopes (equipped/enchanted) never cover the source.
     */
    private boolean selfInScope(StaticEffectContext context, GrantScope scope, PermanentPredicate filter) {
        if (scope == GrantScope.SELF) return true;
        boolean selfCoveringScope = scope == GrantScope.ALL_OWN_CREATURES
                || scope == GrantScope.ALL_CREATURES
                || scope == GrantScope.OWN_PERMANENTS;
        return selfCoveringScope && matchesStaticFilter(context.target(), filter);
    }

    public boolean isEquipped(StaticEffectContext context) {
        for (UUID playerId : context.gameData().orderedPlayerIds) {
            List<Permanent> bf = context.gameData().playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent permanent : bf) {
                if (permanent.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(context.target().getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isControllerLifeAtOrBelow(StaticEffectContext context, int threshold) {
        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) return false;
        int lifeTotal = context.gameData().playerLifeTotals.getOrDefault(controllerId, 20);
        return lifeTotal <= threshold;
    }

    public boolean isTopCardOfLibraryColor(StaticEffectContext context, CardColor color) {
        UUID controllerId = findControllerId(context.gameData(), context.source());
        if (controllerId == null) return false;
        List<Card> deck = context.gameData().playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) return false;
        return deck.getFirst().getColors().contains(color);
    }

    public int countControlledPermanents(StaticEffectContext context, Predicate<Permanent> filter) {
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

    public UUID findControllerId(GameData gameData, Permanent permanent) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.contains(permanent)) {
                return playerId;
            }
        }
        return null;
    }

    public boolean matchesStaticFilter(Permanent target, PermanentPredicate filter) {
        if (filter == null) return true;
        if (filter instanceof PermanentColorInPredicate p) {
            if (target.isColorOverridden()) {
                return target.getTransientColors().stream().anyMatch(p.colors()::contains);
            }
            CardColor effectiveColor = target.getEffectiveColor();
            return (effectiveColor != null && p.colors().contains(effectiveColor))
                    || target.getTransientColors().stream().anyMatch(p.colors()::contains);
        }
        if (filter instanceof PermanentHasSubtypePredicate p)
            return target.getCard().getSubtypes().contains(p.subtype())
                    || target.getTransientSubtypes().contains(p.subtype())
                    || target.getGrantedSubtypes().contains(p.subtype())
                    || (isCreatureSubtype(p.subtype()) && target.hasKeyword(Keyword.CHANGELING));
        if (filter instanceof PermanentHasAnySubtypePredicate p)
            return target.getCard().getSubtypes().stream().anyMatch(p.subtypes()::contains)
                    || target.getTransientSubtypes().stream().anyMatch(p.subtypes()::contains)
                    || target.getGrantedSubtypes().stream().anyMatch(p.subtypes()::contains)
                    || (p.subtypes().stream().anyMatch(StaticEffectSupport::isCreatureSubtype)
                    && target.hasKeyword(Keyword.CHANGELING));
        if (filter instanceof PermanentHasKeywordPredicate p)
            return target.hasKeyword(p.keyword());
        if (filter instanceof PermanentIsCreaturePredicate)
            return target.getCard().hasType(CardType.CREATURE)
                    || target.isAnimatedUntilEndOfTurn()
                    || target.isAnimatedUntilNextTurn()
                    || target.getCounterCount(CounterType.AWAKENING) > 0;
        if (filter instanceof PermanentIsArtifactPredicate)
            return gameQueryService.isArtifact(target);
        if (filter instanceof PermanentIsLandPredicate)
            return target.getCard().hasType(CardType.LAND);
        if (filter instanceof PermanentIsPlaneswalkerPredicate)
            return target.getCard().hasType(CardType.PLANESWALKER);
        if (filter instanceof PermanentIsTokenPredicate)
            return target.getCard().isToken();
        if (filter instanceof PermanentIsHistoricPredicate)
            return gameQueryService.isArtifact(target)
                    || target.getCard().getSupertypes().contains(CardSupertype.LEGENDARY)
                    || target.getCard().getSubtypes().contains(CardSubtype.SAGA)
                    || target.getTransientSubtypes().contains(CardSubtype.SAGA);
        if (filter instanceof PermanentNotPredicate p)
            return !matchesStaticFilter(target, p.predicate());
        if (filter instanceof PermanentAllOfPredicate p)
            return p.predicates().stream().allMatch(inner -> matchesStaticFilter(target, inner));
        if (filter instanceof PermanentAnyOfPredicate p)
            return p.predicates().stream().anyMatch(inner -> matchesStaticFilter(target, inner));
        if (filter instanceof PermanentHasSupertypePredicate p)
            return target.getCard().getSupertypes().contains(p.supertype());
        if (filter instanceof PermanentIsAttackingPredicate)
            return target.isAttacking();
        if (filter instanceof PermanentTruePredicate) return true;
        if (filter instanceof PermanentPowerAtMostPredicate p)
            return target.getEffectivePower() <= p.maxPower();
        if (filter instanceof PermanentToughnessAtMostPredicate p)
            return target.getEffectiveToughness() <= p.maxToughness();
        if (filter instanceof PermanentPowerAtLeastPredicate p)
            return target.getEffectivePower() >= p.minPower();
        if (filter instanceof PermanentHasCountersPredicate p)
            return switch (p.counterType()) {
                case PLUS_ONE_PLUS_ONE -> target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > 0;
                case MINUS_ONE_MINUS_ONE -> target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) > 0;
                case CHARGE -> target.getCounterCount(CounterType.CHARGE) > 0;
                case LOYALTY -> target.getCounterCount(CounterType.LOYALTY) > 0;
                case HATCHLING -> target.getCounterCount(CounterType.HATCHLING) > 0;
                case SLIME -> target.getCounterCount(CounterType.SLIME) > 0;
                case STUDY -> target.getCounterCount(CounterType.STUDY) > 0;
                case WISH -> target.getCounterCount(CounterType.WISH) > 0;
                case LORE -> target.getCounterCount(CounterType.LORE) > 0;
                case AIM -> target.getCounterCount(CounterType.AIM) > 0;
                case ANY -> target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > 0
                        || target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) > 0
                        || target.getCounterCount(CounterType.CHARGE) > 0
                        || target.getCounterCount(CounterType.LOYALTY) > 0
                        || target.getCounterCount(CounterType.HATCHLING) > 0
                        || target.getCounterCount(CounterType.SLIME) > 0
                        || target.getCounterCount(CounterType.STUDY) > 0
                        || target.getCounterCount(CounterType.WISH) > 0
                        || target.getCounterCount(CounterType.LORE) > 0
                        || target.getCounterCount(CounterType.AIM) > 0;
                default -> false;
            };
        throw new IllegalArgumentException("Unsupported static filter predicate: " + filter.getClass().getSimpleName());
    }

    public static boolean isCreatureSubtype(CardSubtype subtype) {
        return !NON_CREATURE_SUBTYPES.contains(subtype);
    }

    public int countCardsInAllGraveyards(GameData gameData, CardPredicate filter) {
        int count = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (card.isToken()) continue;
                if (predicateEvaluationService.matchesCardPredicate(card, filter, null)) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean hasAnimateArtifactEffect(GameData gameData) {
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
