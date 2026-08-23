package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AllLandsAreCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Shared helpers used by every static effect handler. Per-effect {@link StaticEffectHandlerBean}
 * handlers reuse these helpers; behavior is identical to the original monolith privates.
 */
@Component
public class StaticEffectSupport {

    private final GameQueryService gameQueryService;

    private final AmountEvaluationService amountEvaluationService;

    @Autowired
    public StaticEffectSupport(GameQueryService gameQueryService, AmountEvaluationService amountEvaluationService) {
        this.gameQueryService = gameQueryService;
        this.amountEvaluationService = amountEvaluationService;
    }

    /**
     * Evaluates the filter predicates the handlers pass in. Injected lazily because the
     * evaluation service queries game state through {@link GameQueryService}, which reaches
     * the static effect handlers that own this support bean.
     */
    @Autowired
    @Lazy
    private PredicateEvaluationService predicateEvaluationService;

    private static final PermanentIsLandPredicate LAND_PREDICATE = new PermanentIsLandPredicate();

    private static final Set<CardSubtype> NON_CREATURE_SUBTYPES = EnumSet.of(
            CardSubtype.FOREST,
            CardSubtype.MOUNTAIN,
            CardSubtype.ISLAND,
            CardSubtype.PLAINS,
            CardSubtype.SWAMP,
            CardSubtype.DESERT,
            CardSubtype.GATE,
            CardSubtype.LOCUS,
            CardSubtype.AURA,
            CardSubtype.EQUIPMENT,
            CardSubtype.AJANI,
            CardSubtype.KOTH,
            CardSubtype.BOLAS
    );

    /**
     * Returns true if the target matches the given creature-centric scope.
     * Handles ENCHANTED_CREATURE, ENCHANTED_PERMANENT, EQUIPPED_CREATURE, OWN_TAPPED_CREATURES, OWN_UNTAPPED_CREATURES, OWN_CREATURES, ALL_OWN_CREATURES, ALL_CREATURES, and OWN_PERMANENTS.
     */
    public boolean matchesCreatureScope(StaticEffectContext context, GrantScope scope, PermanentPredicate filter) {
        if (scope == GrantScope.OWN_PERMANENTS) {
            return context.targetOnSameBattlefield()
                    && matchesStaticFilter(context, context.target(), filter);
        }
        if (scope == GrantScope.ENCHANTED_CREATURE || scope == GrantScope.ENCHANTED_PERMANENT || scope == GrantScope.EQUIPPED_CREATURE) {
            return context.source().isAttached()
                    && context.source().getAttachedTo().equals(context.target().getId())
                    && matchesStaticFilter(context, context.target(), filter);
        }
        if (scope == GrantScope.ENCHANTED_PLAYER_CREATURES) {
            if (!context.source().isAttached()) return false;
            UUID attachedPlayerId = context.source().getAttachedTo();
            List<Permanent> attachedPlayerBf = context.gameData().playerBattlefields.get(attachedPlayerId);
            if (attachedPlayerBf == null || !attachedPlayerBf.contains(context.target())) return false;
            boolean hasAnimateArtifacts = hasAnimateArtifactEffect(context.gameData());
            return isEffectivelyCreature(context.gameData(), context.target(), hasAnimateArtifacts)
                    && matchesStaticFilter(context, context.target(), filter);
        }
        if (scope == GrantScope.SELF_AND_PAIRED) {
            UUID pairedId = context.source().getPairedWithId();
            return context.target().getId().equals(context.source().getId())
                    || (pairedId != null && context.target().getId().equals(pairedId));
        }
        if (scope == GrantScope.OWN_TAPPED_CREATURES) {
            return context.targetOnSameBattlefield() && context.target().isTapped();
        }
        if (scope == GrantScope.OWN_UNTAPPED_CREATURES) {
            if (!context.targetOnSameBattlefield() || context.target().isTapped()) return false;
            boolean hasAnimateArtifacts = hasAnimateArtifactEffect(context.gameData());
            return isEffectivelyCreature(context.gameData(), context.target(), hasAnimateArtifacts);
        }
        if (scope == GrantScope.OWN_CREATURES || scope == GrantScope.ALL_OWN_CREATURES
                || scope == GrantScope.OPPONENT_CREATURES || scope == GrantScope.ALL_CREATURES
                || scope == GrantScope.ALL_CREATURES_INCLUDING_SELF) {
            if ((scope == GrantScope.OWN_CREATURES || scope == GrantScope.ALL_CREATURES)
                    && context.target().getId().equals(context.source().getId())) {
                return false;
            }
            boolean ownCheck = scope == GrantScope.ALL_CREATURES
                    || scope == GrantScope.ALL_CREATURES_INCLUDING_SELF
                    || (scope == GrantScope.OWN_CREATURES && context.targetOnSameBattlefield())
                    || (scope == GrantScope.ALL_OWN_CREATURES && context.targetOnSameBattlefield())
                    || (scope == GrantScope.OPPONENT_CREATURES && !context.targetOnSameBattlefield());
            if (!ownCheck) return false;
            boolean hasAnimateArtifacts = hasAnimateArtifactEffect(context.gameData());
            return isEffectivelyCreature(context.gameData(), context.target(), hasAnimateArtifacts)
                    && matchesStaticFilter(context, context.target(), filter);
        }
        return false;
    }

    /**
     * Returns true if the target matches a land-centric scope ({@link GrantScope#OWN_LANDS} /
     * {@link GrantScope#OPPONENT_LANDS} /
     * {@link GrantScope#ALL_LANDS}) and the optional filter. The land check goes through the
     * recursion-safe static matcher rather than {@code GameQueryService}, which would re-enter
     * static-bonus assembly from inside the static pass.
     */
    public boolean matchesLandScope(StaticEffectContext context, GrantScope scope, PermanentPredicate filter) {
        if (scope == GrantScope.OWN_LANDS && !context.targetOnSameBattlefield()) {
            return false;
        }
        if (scope == GrantScope.OPPONENT_LANDS && context.targetOnSameBattlefield()) {
            return false;
        }
        return predicateEvaluationService.matchesStaticLeaf(context.target(), LAND_PREDICATE)
                && matchesStaticFilter(context, context.target(), filter);
    }

    public boolean isEffectivelyCreature(Permanent permanent, boolean hasAnimateArtifacts) {
        return isEffectivelyCreature(null, permanent, hasAnimateArtifacts);
    }

    public boolean isEffectivelyCreature(GameData gameData, Permanent permanent, boolean hasAnimateArtifacts) {
        if (permanent.getCard().hasType(CardType.CREATURE)) return true;
        if (permanent.isAnimatedUntilEndOfTurn()) return true;
        if (permanent.isAnimatedUntilEndOfCombat()) return true;
        if (permanent.isAnimatedUntilNextTurn()) return true;
        if (permanent.getCounterCount(CounterType.AWAKENING) > 0) return true;
        if (hasAnimateArtifacts && gameQueryService.isArtifact(permanent)) return true;
        if (gameData != null && permanent.getCard().hasType(CardType.LAND)
                && matchesAnimateLand(gameData, permanent)) return true;
        if (gameData != null && gameQueryService.isAnimatedByStarfield(gameData, permanent)) return true;
        if (gameData != null) return gameQueryService.hasSelfBecomeCreatureEffect(gameData, permanent);
        return false;
    }

    public void applySelfOnlyConditionalStaticEffect(StaticEffectContext context, CardEffect wrapped, StaticBonusAccumulator accumulator) {
        if (wrapped instanceof StaticBoostEffect boost) {
            if (selfInScope(context, boost.scope(), boost.filter())) {
                int multiplier = boost.scalingCounter() == null
                        ? 1
                        : (boost.scalingCounterOnTarget()
                                ? context.target().getCounterCount(boost.scalingCounter())
                                : context.source().getCounterCount(boost.scalingCounter()));
                accumulator.addPower(boost.powerBoost() * multiplier);
                accumulator.addToughness(boost.toughnessBoost() * multiplier);
                accumulator.addKeywords(boost.grantedKeywords());
            }
        } else if (wrapped instanceof BoostSelfEffect boost) {
            AmountContext amountContext =
                    AmountContext.forStaticEffect(context.source(), context.sourceControllerId());
            accumulator.addPower(amountEvaluationService.evaluate(context.gameData(), boost.powerBoost(), amountContext));
            accumulator.addToughness(amountEvaluationService.evaluate(context.gameData(), boost.toughnessBoost(), amountContext));
        } else if (wrapped instanceof GrantKeywordEffect grant) {
            if (grant.scope() == GrantScope.SELF_AND_PAIRED
                    || selfInScope(context, grant.scope(), grant.filter())) {
                accumulator.addKeywords(grant.keywords());
            }
        } else if (wrapped instanceof GrantSubtypeEffect grant) {
            if (selfInScope(context, grant.scope(), grant.filter())) {
                accumulator.addGrantedSubtype(grant.subtype());
                if (grant.overriding()) {
                    accumulator.setSubtypeOverriding(true);
                }
            }
        } else if (wrapped instanceof GrantActivatedAbilityEffect grant) {
            if (grant.scope() == GrantScope.SELF || grant.scope() == GrantScope.SELF_AND_PAIRED
                    || grant.scope() == GrantScope.ALL_OWN_CREATURES
                    || grant.scope() == GrantScope.OWN_PERMANENTS) {
                accumulator.addActivatedAbility(grant.ability().withGrantSource(context.source().getId()));
            }
        } else if (wrapped instanceof GrantColorEffect grant) {
            if (grant.scope() == GrantScope.SELF || grant.scope() == GrantScope.SELF_AND_PAIRED
                    || selfInScope(context, grant.scope(), grant.filter())) {
                accumulator.addGrantedColor(grant.color());
                if (grant.overriding()) {
                    accumulator.setColorOverriding(true);
                }
            }
        } else if (wrapped instanceof GrantTriggeredAbilityEffect grant) {
            if (grant.scope() == GrantScope.SELF || grant.scope() == GrantScope.SELF_AND_PAIRED
                    || selfInScope(context, grant.scope(), grant.filter())) {
                accumulator.addGrantedEffect(grant);
            }
        } else if (wrapped instanceof ProtectionFromColorsEffect protection) {
            accumulator.addProtectionColors(protection.colors());
        } else if (wrapped instanceof SetBasePowerToughnessEffect setPT
                && setPT.scope() == GrantScope.SELF) {
            accumulator.setBasePTOverride(setPT.power(), setPT.toughness());
        } else if (wrapped instanceof GrantEffectEffect grant) {
            if (grant.scope() == GrantScope.SELF || grant.scope() == GrantScope.SELF_AND_PAIRED
                    || matchesStaticFilter(context, context.target(), grant.filter())) {
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
        } else if (wrapped instanceof SetCardTypesEffect set && set.scope() == GrantScope.SELF) {
            accumulator.setCardTypeOverriding(true);
            accumulator.setGrantedCardTypes(set.cardTypes());
        }
    }

    /**
     * Whether a conditional static effect's scope covers the source permanent itself.
     * {@link GrantScope#OWN_CREATURES} means "other creatures you control" and is excluded;
     * attachment scopes (equipped/enchanted) never cover the source.
     */
    private boolean selfInScope(StaticEffectContext context, GrantScope scope, PermanentPredicate filter) {
        if (scope == GrantScope.SELF || scope == GrantScope.SELF_AND_PAIRED) return true;
        boolean selfCoveringScope = scope == GrantScope.ALL_OWN_CREATURES
                || scope == GrantScope.ALL_CREATURES
                || scope == GrantScope.ALL_CREATURES_INCLUDING_SELF
                || scope == GrantScope.OWN_PERMANENTS;
        return selfCoveringScope && matchesStaticFilter(context, context.target(), filter);
    }

    /**
     * The handlers' entry point to {@link PredicateEvaluationService#matchesStaticFilter}, which
     * owns the recursion-safe evaluation. Target is explicit rather than taken from the context
     * because several handlers filter permanents other than the one being assembled (Grant Equip
     * by Mana Value scans everything attached to its target).
     */
    public boolean matchesStaticFilter(StaticEffectContext context, Permanent target, PermanentPredicate filter) {
        return predicateEvaluationService.matchesStaticFilter(target, filter, filterContextOf(context));
    }

    /**
     * The handlers' entry point to {@link PredicateEvaluationService#matchesStaticLeaf}, for a
     * single predicate the handler builds itself rather than one taken from the card's ability.
     */
    public boolean matchesStaticLeaf(Permanent target, PermanentPredicate leaf) {
        return predicateEvaluationService.matchesStaticLeaf(target, leaf);
    }

    /**
     * The board shape and source identity the four board-reading filter predicates need. The
     * source is identified by its current card id, which is what a clone's filter must match on.
     */
    private static FilterContext filterContextOf(StaticEffectContext context) {
        return FilterContext.of(context.gameData())
                .withSourceCardId(context.source().getCard().getId())
                .withSourceControllerId(context.sourceControllerId())
                .withSourcePermanentSnapshot(context.source());
    }

    public static boolean isCreatureSubtype(CardSubtype subtype) {
        return !NON_CREATURE_SUBTYPES.contains(subtype);
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

    public boolean matchesAnimateLand(GameData gameData, Permanent permanent) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent source : bf) {
                for (CardEffect e : source.getCard().getEffects(EffectSlot.STATIC)) {
                    if (e instanceof AllLandsAreCreaturesEffect animateLands
                            && (animateLands.scope() == GrantScope.ALL_LANDS
                                    || (animateLands.scope() == GrantScope.OWN_LANDS
                                            && bf.contains(permanent))
                                    || (animateLands.scope() == GrantScope.OPPONENT_LANDS
                                            && !bf.contains(permanent)))
                            && (animateLands.requiredSubtype() == null
                                    || permanent.getCard().getSubtypes().contains(animateLands.requiredSubtype()))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
