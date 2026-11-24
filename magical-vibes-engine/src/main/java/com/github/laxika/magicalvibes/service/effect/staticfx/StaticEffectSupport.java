package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AllLandsAreCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetNameEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.CharacteristicState;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import com.github.laxika.magicalvibes.service.effect.TextChangeTransformer;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Shared helpers used by every static effect handler. Per-effect {@link StaticEffectHandlerBean}
 * handlers reuse these helpers; behavior is identical to the original monolith privates.
 */
@Component
public class StaticEffectSupport implements SubjectResolutionService.StaticReads {

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

    /**
     * Resolves the handlers' subjects (the resolver's static entry point, asked about one permanent). Injected lazily
     * for the same reason as the evaluation service.
     */
    @Autowired
    @Lazy
    private SubjectResolutionService subjectResolutionService;

    /** "Enchanted permanent", the host whatever it is. */
    public static final PermanentSubject ATTACHED = Subjects.attached();
    /** "Other creatures you control". */
    public static final PermanentSubject OTHER_CREATURES_YOU_CONTROL = Subjects.otherCreaturesYouControl();
    /** "Lands you control". */
    public static final PermanentSubject LANDS_YOU_CONTROL = Subjects.landsYouControl();

    /**
     * Whether a static ability's {@code subject} names the context's target and the target matches {@code filter}:
     * {@link SubjectResolutionService#names(StaticEffectContext, PermanentSubject, PermanentPredicate,
     * SubjectResolutionService.StaticReads)}, with this support's recursion-safe reads.
     */
    public boolean names(StaticEffectContext context, PermanentSubject subject, PermanentPredicate filter) {
        return subjectResolutionService.names(context, subject, filter, this);
    }

    /**
     * {@link #names} for a handler that applies a static ability to permanents other than its source: a
     * {@code source()} subject is the effect's self handler's (the source's own abilities are assembled apart), so
     * this answers no for it even when the assembly hands the handler the source itself (a static ability granted to
     * the source).
     */
    public boolean namesBeyondSource(StaticEffectContext context, PermanentSubject subject,
                                     PermanentPredicate filter) {
        return !(subject instanceof PermanentSubject.Source) && names(context, subject, filter);
    }

    @Override
    public boolean isCreature(GameData gameData, Permanent permanent) {
        return isEffectivelyCreature(gameData, permanent, hasAnimateArtifactEffect(gameData));
    }

    @Override
    public boolean matchesLeaf(Permanent permanent, PermanentPredicate leaf) {
        return predicateEvaluationService.matchesStaticLeaf(permanent, leaf);
    }

    @Override
    public boolean matchesFilter(StaticEffectContext context, Permanent permanent, PermanentPredicate filter) {
        return matchesStaticFilter(context, permanent, filter);
    }

    public boolean isEffectivelyCreature(GameData gameData, Permanent permanent, boolean hasAnimateArtifacts) {
        if (permanent.isFaceDown()) return true;
        CharacteristicState activeState = LayerSystemService.activeStateFor(permanent.getId());
        if (activeState != null && activeState.hasCardType(CardType.CREATURE)) return true;
        if (activeState == null && permanent.getCard().hasType(CardType.CREATURE)) return true;
        if (gameData != null && gameData.animatedByFloatingEffect(permanent.getId())) return true;
        if (permanent.getCounterCount(CounterType.AWAKENING) > 0) return true;
        // With a layered state the animation's layer-4 type change is in it (CR 613.1d); without one its filter is
        // judged on the permanent's own characteristics.
        if (hasAnimateArtifacts && activeState == null && gameData != null
                && gameQueryService.isAnimatedByPrintedStaticAnimation(gameData, permanent,
                permanent.getCard().getId(), false)) {
            return true;
        }
        boolean land = activeState != null
                ? activeState.hasCardType(CardType.LAND)
                : permanent.getCard().hasType(CardType.LAND);
        if (gameData != null && land
                && matchesAnimateLand(gameData, permanent,
                        activeState != null ? activeState.getSubtypes() : permanent.getCard().getSubtypes())) {
            return true;
        }
        if (gameData != null && gameQueryService.isAnimatedByStarfield(gameData, permanent)) return true;
        if (gameData != null) return gameQueryService.hasSelfBecomeCreatureEffect(gameData, permanent);
        return false;
    }

    /**
     * Returns a permanent's current effective color count, including layer-5 color changes while
     * the layered pass is active.
     */
    public int effectiveColorCount(Permanent permanent) {
        CharacteristicState layered = LayerSystemService.activeStateFor(permanent.getId());
        if (layered != null) {
            return layered.getColors().size();
        }
        Set<CardColor> colors = new HashSet<>();
        colors.addAll(permanent.getEffectiveColors());
        return colors.size();
    }

    public void applySelfOnlyConditionalStaticEffect(StaticEffectContext context, CardEffect wrapped, StaticBonusAccumulator accumulator) {
        if (wrapped instanceof StaticBoostEffect boost) {
            if (names(context, boost.subject(), boost.filter())) {
                int multiplier = boost.scalingCounter() == null
                        ? 1
                        : (boost.scalingCounterOnTarget()
                                ? context.target().getCounterCount(boost.scalingCounter())
                                : context.sourceCounterCount(boost.scalingCounter()));
                accumulator.addPower(boost.powerBoost() * multiplier);
                accumulator.addToughness(boost.toughnessBoost() * multiplier);
                accumulator.addKeywords(boost.grantedKeywords());
            }
        } else if (wrapped instanceof BoostSelfEffect boost) {
            BoostSelfSelfEffectHandler.requireStaticSelfBoost(boost);
            AmountContext amountContext =
                    AmountContext.forStaticEffect(context.source(), context.sourceControllerId());
            accumulator.addPower(amountEvaluationService.evaluate(context.gameData(), boost.powerBoost(), amountContext));
            accumulator.addToughness(amountEvaluationService.evaluate(context.gameData(), boost.toughnessBoost(), amountContext));
        } else if (wrapped instanceof GrantKeywordEffect grant) {
            if (names(context, grant.subject(), grant.filter())) {
                accumulator.addKeywords(grant.keywords());
            }
        } else if (wrapped instanceof GrantSubtypeEffect grant) {
            if (names(context, grant.subject(), grant.filter()) && grant.subtypeFor(context.source()) != null) {
                accumulator.addGrantedSubtype(grant.subtypeFor(context.source()));
                if (grant.overriding()) {
                    accumulator.setSubtypeOverriding(true);
                }
            }
        } else if (wrapped instanceof GrantActivatedAbilityEffect grant) {
            if (names(context, grant.subject(), grant.filter())) {
                accumulator.addActivatedAbility(grant.ability().withGrantSource(context.sourceId()));
            }
        } else if (wrapped instanceof GrantColorEffect grant) {
            CardColor color = grant.ofChosenColor() ? context.source().getChosenColor() : grant.color();
            if (color != null && names(context, grant.subject(), grant.filter())) {
                accumulator.addGrantedColor(color);
                if (grant.overriding()) {
                    accumulator.setColorOverriding(true);
                }
            }
        } else if (wrapped instanceof GrantTriggeredAbilityEffect grant) {
            if (names(context, grant.subject(), grant.filter())) {
                accumulator.addGrantedEffect(grant);
            }
        } else if (wrapped instanceof ProtectionFromColorsEffect protection) {
            accumulator.addProtectionColors(protection.colors());
        } else if (wrapped instanceof RemoveKeywordEffect remove
                && names(context, remove.subject(), remove.filter())) {
            accumulator.removeKeyword(remove.keyword());
        } else if (wrapped instanceof SetBasePowerToughnessEffect setPT
                && names(context, setPT.subject(), setPT.filter())) {
            accumulator.setBasePTOverride(setPT.power(), setPT.toughness());
        } else if (wrapped instanceof SetNameEffect setName
                && names(context, setName.subject(), null)) {
            accumulator.setName(setName.name());
        } else if (wrapped instanceof GrantEffectEffect grant) {
            if (names(context, grant.subject(), grant.filter())) {
                accumulator.addGrantedEffect(grant.effect());
            }
        } else if (wrapped instanceof AnimatePermanentsEffect animate
                && names(context, animate.subject(), animate.filter())) {
            accumulator.setSelfBecomeCreature(true);
            if (animate.cardTypeOverriding()) {
                java.util.Set<CardType> types = java.util.EnumSet.of(CardType.CREATURE);
                types.addAll(animate.grantedCardTypes());
                accumulator.setCardTypeOverriding(true);
                accumulator.setGrantedCardTypes(types);
            }
            // Conditional self-become-creature statics (Rusted Relic, Warden of the Wall) always use
            // a fixed base P/T; static bonus computation has no stack entry to evaluate a dynamic amount.
            if (animate.power() instanceof Fixed p && animate.toughness() instanceof Fixed t) {
                // Base P/T in layer 7b (CR 613.4b), not a modifier, so later setters override it.
                accumulator.setBasePTOverride(p.value(), t.value());
            }
            for (CardSubtype subtype : animate.grantedSubtypes()) {
                accumulator.addGrantedSubtype(subtype);
            }
            accumulator.addKeywords(animate.grantedKeywords());
        } else if (wrapped instanceof SetCardTypesEffect set
                && names(context, set.subject(), null)
                && (!Duration.WHILE_ATTACHED.equals(set.duration()) || context.source().isAttached())) {
            accumulator.setCardTypeOverriding(true);
            accumulator.setGrantedCardTypes(set.cardTypes());
        }
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

    /** Matches a card predicate using the source-relative context of a static effect. */
    public boolean matchesCardFilter(StaticEffectContext context, Card target, CardPredicate filter) {
        return predicateEvaluationService.matchesCardPredicate(
                target, filter, context.sourceCard().getId(), context.gameData(), context.sourceControllerId());
    }

    /**
     * The handlers' entry point to {@link PredicateEvaluationService#matchesStaticLeaf}, for a
     * single predicate the handler builds itself rather than one taken from the card's ability.
     */
    public boolean matchesStaticLeaf(Permanent target, PermanentPredicate leaf) {
        return predicateEvaluationService.matchesStaticLeaf(target, leaf);
    }

    public boolean sharesCreatureType(Permanent first, Permanent second) {
        return predicateEvaluationService.sharesCreatureType(first, second);
    }

    /**
     * The board shape and source identity the four board-reading filter predicates need. The
     * source is identified by its current card id, which is what a clone's filter must match on.
     */
    private static FilterContext filterContextOf(StaticEffectContext context) {
        return FilterContext.of(context.gameData())
                .withSourceCardId(context.sourceCard().getId())
                .withSourceControllerId(context.sourceControllerId())
                .withSourcePermanentSnapshot(context.source());
    }

    public static boolean isCreatureSubtype(CardSubtype subtype) {
        return subtype.isCreatureType();
    }

    /**
     * Whether a face-up battlefield permanent prints an {@link AnimateNoncreatureArtifactsEffect} (March of the
     * Machines, Opalescence): the cheap gate before {@link #isEffectivelyCreature} judges what it animates. A
     * face-down one has no abilities (CR 708.2).
     */
    public boolean hasAnimateArtifactEffect(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent source : bf) {
                if (source.isFaceDown()) continue;
                for (CardEffect e : source.getCard().getStaticEffects()) {
                    if (e instanceof AnimateNoncreatureArtifactsEffect) return true;
                }
            }
        }
        return false;
    }

    /**
     * Whether a face-up battlefield permanent's {@link AllLandsAreCreaturesEffect} animates {@code permanent}, a land
     * with {@code subtypes}, reading the required land type through the source's own text changes (Magical Hack).
     * A face-down source has no abilities (CR 708.2).
     */
    public boolean matchesAnimateLand(GameData gameData, Permanent permanent,
                                      Collection<CardSubtype> subtypes) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent source : bf) {
                if (source.isFaceDown()) continue;
                for (CardEffect printed : source.getCard().getStaticEffects()) {
                    if (!(printed instanceof AllLandsAreCreaturesEffect)) continue;
                    CardEffect e = TextChangeTransformer.transform(printed, gameData.textReplacements(source.getId()));
                    if (e instanceof AllLandsAreCreaturesEffect animateLands
                            && subjectResolutionService.names(gameData, source, playerId,
                                    bf.contains(permanent), permanent, animateLands.subject(), this)
                            && (animateLands.requiredSubtype() == null
                                    || subtypes.contains(animateLands.requiredSubtype()))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
