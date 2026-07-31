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
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControllerControlsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestManaValueAmongAllCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.CharacteristicState;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class StaticEffectSupport {

    private final GameQueryService gameQueryService;

    /**
     * Evaluates the filter predicates the handlers pass in. Injected lazily because the
     * evaluation service queries game state through {@link GameQueryService}, which reaches
     * the static effect handlers that own this support bean.
     */
    @Autowired
    @Lazy
    private PredicateEvaluationService predicateEvaluationService;

    private static final PermanentIsCreaturePredicate CREATURE_PREDICATE = new PermanentIsCreaturePredicate();

    private static final Set<CardSubtype> NON_CREATURE_SUBTYPES = EnumSet.of(
            CardSubtype.FOREST,
            CardSubtype.MOUNTAIN,
            CardSubtype.ISLAND,
            CardSubtype.PLAINS,
            CardSubtype.SWAMP,
            CardSubtype.AURA,
            CardSubtype.EQUIPMENT,
            CardSubtype.AJANI,
            CardSubtype.KOTH,
            CardSubtype.BOLAS
    );

    /**
     * Returns true if the target matches the given creature-centric scope.
     * Handles ENCHANTED_CREATURE, ENCHANTED_PERMANENT, EQUIPPED_CREATURE, OWN_TAPPED_CREATURES, OWN_UNTAPPED_CREATURES, OWN_CREATURES, ALL_OWN_CREATURES, ALL_CREATURES.
     */
    public boolean matchesCreatureScope(StaticEffectContext context, GrantScope scope, PermanentPredicate filter) {
        if (scope == GrantScope.ENCHANTED_CREATURE || scope == GrantScope.ENCHANTED_PERMANENT || scope == GrantScope.EQUIPPED_CREATURE) {
            return context.source().isAttached()
                    && context.source().getAttachedTo().equals(context.target().getId())
                    && matchesStaticFilter(context, filter);
        }
        if (scope == GrantScope.ENCHANTED_PLAYER_CREATURES) {
            if (!context.source().isAttached()) return false;
            UUID attachedPlayerId = context.source().getAttachedTo();
            List<Permanent> attachedPlayerBf = context.gameData().playerBattlefields.get(attachedPlayerId);
            if (attachedPlayerBf == null || !attachedPlayerBf.contains(context.target())) return false;
            boolean hasAnimateArtifacts = hasAnimateArtifactEffect(context.gameData());
            return isEffectivelyCreature(context.gameData(), context.target(), hasAnimateArtifacts)
                    && matchesStaticFilter(context, filter);
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
            boolean ownCheck = scope == GrantScope.ALL_CREATURES
                    || scope == GrantScope.ALL_CREATURES_INCLUDING_SELF
                    || (scope == GrantScope.OWN_CREATURES && context.targetOnSameBattlefield())
                    || (scope == GrantScope.ALL_OWN_CREATURES && context.targetOnSameBattlefield())
                    || (scope == GrantScope.OPPONENT_CREATURES && !context.targetOnSameBattlefield());
            if (!ownCheck) return false;
            boolean hasAnimateArtifacts = hasAnimateArtifactEffect(context.gameData());
            return isEffectivelyCreature(context.gameData(), context.target(), hasAnimateArtifacts)
                    && matchesStaticFilter(context, filter);
        }
        return false;
    }

    /**
     * Returns true if the target matches a land-centric scope ({@link GrantScope#OWN_LANDS} /
     * {@link GrantScope#ALL_LANDS}) and the optional filter. The land check goes through the
     * recursion-safe static matcher rather than {@code GameQueryService}, which would re-enter
     * static-bonus assembly from inside the static pass.
     */
    public boolean matchesLandScope(StaticEffectContext context, GrantScope scope, PermanentPredicate filter) {
        if (scope == GrantScope.OWN_LANDS && !context.targetOnSameBattlefield()) {
            return false;
        }
        return matchesStaticFilter(context.target(), new PermanentIsLandPredicate())
                && matchesStaticFilter(context, filter);
    }

    /**
     * Context-aware static-filter check. Handles predicates that need game data (e.g.
     * {@link PermanentHasGreatestManaValueAmongAllCreaturesPredicate}) and delegates everything
     * else to the target-only {@link #matchesStaticFilter(Permanent, PermanentPredicate)}.
     */
    private boolean matchesStaticFilter(StaticEffectContext context, PermanentPredicate filter) {
        if (filter instanceof PermanentHasGreatestManaValueAmongAllCreaturesPredicate) {
            return hasGreatestManaValueAmongAllCreaturesStatic(context.gameData(), context.target());
        }
        if (filter instanceof PermanentIsEnchantedPredicate) {
            return gameQueryService.isEnchanted(context.gameData(), context.target());
        }
        if (filter instanceof PermanentControllerControlsPermanentPredicate p) {
            return controllerControlsMatchingStatic(context.gameData(), context.target(), p);
        }
        if (filter instanceof PermanentHasSourceChosenSubtypePredicate) {
            CardSubtype chosenSubtype = context.source().getChosenSubtype();
            return chosenSubtype != null
                    && matchesStaticFilter(context.target(), new PermanentHasSubtypePredicate(chosenSubtype));
        }
        return matchesStaticFilter(context.target(), filter);
    }

    /**
     * Recursion-safe "the target's own controller controls a matching permanent" for the static
     * pass (Favorable Destiny's "as long as its controller controls another creature"). The inner
     * filter is evaluated with the layer-safe {@link #matchesStaticFilter(Permanent, PermanentPredicate)}
     * rather than the fully layered predicate evaluator, which would re-enter static assembly.
     */
    private boolean controllerControlsMatchingStatic(GameData gameData, Permanent target,
                                                     PermanentControllerControlsPermanentPredicate predicate) {
        if (gameData == null) return false;
        UUID controllerId = gameData.findControllerOf(target.getId());
        List<Permanent> battlefield = controllerId == null ? null : gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        return battlefield.stream()
                .filter(candidate -> !predicate.excludeSelf() || !candidate.getId().equals(target.getId()))
                .anyMatch(candidate -> matchesStaticFilter(candidate, predicate.filter()));
    }

    /**
     * Recursion-safe "greatest mana value among all creatures" (Favor of the Mighty) for the
     * static pass. {@link GameQueryService#hasGreatestManaValueAmongAllCreatures} calls the fully
     * layered {@code isCreature}, which re-enters static-bonus assembly and recurses forever when
     * invoked from a static handler; use the recursion-safe creature matcher instead. Mana value is
     * a copiable characteristic unaffected by layer 7, so the printed value is authoritative.
     */
    private boolean hasGreatestManaValueAmongAllCreaturesStatic(GameData gameData, Permanent target) {
        if (gameData == null || !matchesStaticFilter(target, CREATURE_PREDICATE)) {
            return false;
        }
        int greatest = -1;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent candidate : battlefield) {
                if (matchesStaticFilter(candidate, CREATURE_PREDICATE)) {
                    greatest = Math.max(greatest, candidate.getCard().getManaValue());
                }
            }
        }
        return target.getCard().getManaValue() == greatest;
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
        if (gameData != null) return gameQueryService.hasSelfBecomeCreatureEffect(gameData, permanent, true);
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
            if (grant.scope() == GrantScope.SELF_AND_PAIRED
                    || selfInScope(context, grant.scope(), grant.filter())) {
                accumulator.addKeywords(grant.keywords());
            }
        } else if (wrapped instanceof GrantActivatedAbilityEffect grant) {
            if (grant.scope() == GrantScope.SELF || grant.scope() == GrantScope.SELF_AND_PAIRED
                    || grant.scope() == GrantScope.ALL_OWN_CREATURES
                    || grant.scope() == GrantScope.OWN_PERMANENTS) {
                accumulator.addActivatedAbility(grant.ability().withGrantSource(context.source().getId()));
            }
        } else if (wrapped instanceof GrantTriggeredAbilityEffect grant) {
            if (grant.scope() == GrantScope.SELF || grant.scope() == GrantScope.SELF_AND_PAIRED
                    || selfInScope(context, grant.scope(), grant.filter())) {
                accumulator.addGrantedEffect(grant);
            }
        } else if (wrapped instanceof ProtectionFromColorsEffect protection) {
            accumulator.addProtectionColors(protection.colors());
        } else if (wrapped instanceof GrantEffectEffect grant) {
            if (grant.scope() == GrantScope.SELF || grant.scope() == GrantScope.SELF_AND_PAIRED
                    || matchesStaticFilter(context.target(), grant.filter())) {
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
        if (scope == GrantScope.SELF || scope == GrantScope.SELF_AND_PAIRED) return true;
        boolean selfCoveringScope = scope == GrantScope.ALL_OWN_CREATURES
                || scope == GrantScope.ALL_CREATURES
                || scope == GrantScope.ALL_CREATURES_INCLUDING_SELF
                || scope == GrantScope.OWN_PERMANENTS;
        return selfCoveringScope && matchesStaticFilter(context.target(), filter);
    }

    /**
     * Layer-4-aware artifact check (CR 613.1d) shared by the {@link PermanentIsArtifactPredicate}
     * leaf, the historic leaf and metalcraft. While a CR 613 pass is active the in-flight state
     * already carries type-changing grants such as Silverskin Armor's "equipped creature is an
     * artifact in addition to its other types"; outside a pass it reads the printed type plus the
     * transient and persistent grants stored on the {@link Permanent}.
     *
     * <p>Deliberately bypasses {@link #matchesStaticFilter}: the funnel's CR 613.6 verdict memo
     * is keyed by filter instance, and {@code PermanentIsArtifactPredicate} is a component-less
     * record, so every instance compares equal. Routing an unrelated caller (metalcraft) through
     * it would hand back a verdict memoized for some other ability.
     */
    public boolean isArtifactForStaticFilter(Permanent target) {
        CharacteristicState layered = LayerSystemService.activeStateFor(target.getId());
        if (layered != null) {
            return layered.hasCardType(CardType.ARTIFACT);
        }
        return gameQueryService.isArtifact(target);
    }

    /**
     * The recursion-safe filter funnel every static effect handler goes through. Kept as the
     * handlers' entry point while the four context-needing predicates above still have to be
     * intercepted here; the evaluation itself lives in
     * {@link PredicateEvaluationService#matchesStaticFilter}, which is where the layered
     * evaluation of the same predicates lives too.
     */
    public boolean matchesStaticFilter(Permanent target, PermanentPredicate filter) {
        return predicateEvaluationService.matchesStaticFilter(target, filter);
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
