package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.DeterminedOnce;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.amount.DuringControllerTurn;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.IfSourceAttacking;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.condition.TargetSpellMatches;
import com.github.laxika.magicalvibes.model.effect.AllColorWordsBecomeChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.AllLandsAreCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNonAbilityEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LandwalkIgnoredForBlockingEffect;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.NonbasicLandsBecomeTypeEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerSpellsCantBeCounteredByColorsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTargetingRestrictionToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.trigger.SpellFilter;
import com.github.laxika.magicalvibes.model.trigger.StateTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostControlledSubtypeCountPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Layer 3 (CR 613.1c / 612): text-changing effects rewrite the WORDS of an object's abilities,
 * and everything downstream (layers 4-7, protection, mana abilities) sees the rewritten text.
 *
 * <p>The engine models an ability's words as the color ({@link CardColor}) and basic-land-type
 * ({@link CardSubtype}) parameters of its {@link CardEffect} records — including landwalk
 * keywords, whose land-type word is the subtype {@link Keyword#LANDWALK_MAP} maps them to.
 * This transformer applies a permanent's {@link TextReplacement}s (Mind Bend et al.) to one
 * effect instance, producing a rewritten instance and never mutating the frozen original.
 * Replacements compose in application order: replacement N applies to the output of N-1.
 * A transform that changes nothing returns the SAME instance — the layered pass keys its
 * managed-effect and filter-verdict maps by identity, so untouched effects must stay {@code ==}.
 *
 * <p>The visitor covers the closed set of effect record types with color/basic-land-type
 * parameters that have registered static handlers (plus the wrappers that can carry them);
 * one-shot spell effects are rewritten on the stack when a text-changing spell resolves.
 * Chosen-as-enters colors are stored on
 * the {@code Permanent} and updated directly by the text-change resolution
 * ({@code ChoiceHandlerService}), not rewritten per query. The effect types deliberately NOT
 * handled are listed in {@code agent-docs/LAYER_SYSTEM.md} §7.
 */
public final class TextChangeTransformer {

    /** The lowercase color words a text-change stores in {@link TextReplacement}. */
    private static final Map<String, CardColor> COLOR_WORDS = Map.of(
            "white", CardColor.WHITE,
            "blue", CardColor.BLUE,
            "black", CardColor.BLACK,
            "red", CardColor.RED,
            "green", CardColor.GREEN);

    /** The capitalized basic land type words a text-change stores in {@link TextReplacement}. */
    private static final Map<String, CardSubtype> BASIC_LAND_WORDS = Map.of(
            "Plains", CardSubtype.PLAINS,
            "Island", CardSubtype.ISLAND,
            "Swamp", CardSubtype.SWAMP,
            "Mountain", CardSubtype.MOUNTAIN,
            "Forest", CardSubtype.FOREST);

    private static final Map<String, CardSubtype> CREATURE_TYPE_WORDS = GameQueryService.TEXT_CHANGE_CREATURE_TYPES.stream()
            .collect(java.util.stream.Collectors.toUnmodifiableMap(
                    word -> CardSubtype.valueOf(word).getDisplayName(), CardSubtype::valueOf));

    /** Reverse of {@link Keyword#LANDWALK_MAP}: the walk keyword whose text contains a given
     *  land-type word. */
    private static final Map<CardSubtype, Keyword> LANDWALK_BY_LAND_TYPE = Map.of(
            CardSubtype.FOREST, Keyword.FORESTWALK,
            CardSubtype.MOUNTAIN, Keyword.MOUNTAINWALK,
            CardSubtype.ISLAND, Keyword.ISLANDWALK,
            CardSubtype.SWAMP, Keyword.SWAMPWALK,
            CardSubtype.PLAINS, Keyword.PLAINSWALK);

    private TextChangeTransformer() {
    }

    /** One replacement resolved to the domain values whose word it substitutes; exactly one of
     *  the color pair / land-type pair is non-null (a text change never mixes the two classes). */
    private record Substitution(CardColor fromColor, CardColor toColor,
                                CardSubtype fromLandType, CardSubtype toLandType,
                                CardSubtype fromCreatureType, CardSubtype toCreatureType) {
    }

    /**
     * The replacements a global color-word change (Swirl the Mists) imposes on EVERY object:
     * one entry per color word other than the chosen one, all pointing at the chosen word.
     * Derived from the battlefield on every query rather than stamped onto objects, so the
     * change starts and stops applying as the source enters and leaves.
     *
     * <p>Returns an empty list — the overwhelmingly common case — when no such permanent is on
     * the battlefield, which makes {@link #transform(CardEffect, List, List)} a no-op.
     */
    public static List<TextReplacement> globalColorWordReplacements(GameData gameData) {
        List<Permanent> sources = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (permanent.getChosenColor() != null && permanent.getCard().getStaticEffects().stream()
                        .anyMatch(AllColorWordsBecomeChosenColorEffect.class::isInstance)) {
                    sources.add(permanent);
                }
            }
        }
        if (sources.isEmpty()) {
            return List.of();
        }
        // Layer 3 applies in timestamp order (CR 613.7), so the most recent source's rewrite runs last.
        sources.sort(Comparator.comparingLong(Permanent::getTimestamp));
        List<TextReplacement> replacements = new ArrayList<>();
        for (Permanent source : sources) {
            String chosenWord = source.getChosenColor().name().toLowerCase(Locale.ROOT);
            for (String word : COLOR_WORDS.keySet()) {
                if (!word.equals(chosenWord)) {
                    replacements.add(new TextReplacement(word, chosenWord, false, source.getTimestamp()));
                }
            }
        }
        return replacements;
    }

    /**
     * Applies an object's own text replacements and then any global color-word change, which
     * — rewriting every color word to one word — subsumes whatever the object's own replacements
     * did to color words. An own replacement made after the global change (it carries a later
     * timestamp) is applied after it instead, so it can rewrite the word the global change supplied.
     */
    public static CardEffect transform(CardEffect effect, List<TextReplacement> own,
                                       List<TextReplacement> global) {
        return transform(effect, inOrder(own, global));
    }

    /**
     * Applies an object's own text replacements and any global color-word change, in the order
     * {@link #transform(CardEffect, List, List)} applies them, to the spell a cast trigger's filter asks for ("whenever
     * you cast a blue spell"), returning the same instance when no replacement changes it.
     */
    public static SpellFilter transformSpellFilter(SpellFilter filter, List<TextReplacement> own,
                                                   List<TextReplacement> global) {
        if (filter.spell() == null) {
            return filter;
        }
        CardPredicate spell = filter.spell();
        for (TextReplacement replacement : inOrder(own, global)) {
            Substitution substitution = resolve(replacement);
            if (substitution != null) {
                spell = apply(spell, substitution);
            }
        }
        return spell == filter.spell() ? filter : filter.withSpell(spell);
    }

    /**
     * {@code ability}, a triggered ability of a permanent, read through the permanent's own text replacements and any
     * global color-word change ({@link #transform(CardEffect, List, List)}'s order): its effects, its intervening "if"
     * and event condition, and a state trigger's permanent filters ("when you control no Islands", CR 612.1). Returns
     * the same instance when no replacement changes it.
     */
    public static TriggeredAbility transformTriggeredAbility(TriggeredAbility ability, List<TextReplacement> own,
                                                             List<TextReplacement> global) {
        List<TextReplacement> replacements = inOrder(own, global);
        if (replacements.isEmpty()) {
            return ability;
        }
        List<EffectRegistration> effects = new ArrayList<>(ability.effects().size());
        boolean effectsChanged = false;
        for (EffectRegistration registration : ability.effects()) {
            CardEffect effect = transform(registration.effect(), replacements);
            effectsChanged |= effect != registration.effect();
            effects.add(new EffectRegistration(effect, registration.simultaneousWithPrevious()));
        }
        TriggerOptions options = ability.options();
        Condition interveningIf = transformCondition(options.interveningIf(), replacements);
        Condition eventCondition = transformCondition(options.eventCondition(), replacements);
        boolean optionsChanged = interveningIf != options.interveningIf() || eventCondition != options.eventCondition();
        List<TriggerCondition> conditions = new ArrayList<>(ability.conditions().size());
        boolean conditionsChanged = false;
        for (TriggerCondition condition : ability.conditions()) {
            TriggerCondition transformed = condition.trigger() instanceof StateTrigger state
                    ? transformStateTrigger(condition, state, replacements) : condition;
            conditionsChanged |= transformed != condition;
            conditions.add(transformed);
        }
        if (!effectsChanged && !optionsChanged && !conditionsChanged) {
            return ability;
        }
        return new TriggeredAbility(conditionsChanged ? conditions : ability.conditions(),
                optionsChanged ? new TriggerOptions(interveningIf, options.limit(), options.multiplicity(),
                        eventCondition) : options,
                effectsChanged ? effects : ability.effects(), ability.chapterTargets(), ability.instance(),
                ability.grantingPermanentId(), ability.targetFilter());
    }

    private static Condition transformCondition(Condition condition, List<TextReplacement> replacements) {
        Condition result = condition;
        for (TextReplacement replacement : replacements) {
            Substitution substitution = resolve(replacement);
            if (result != null && substitution != null) {
                result = apply(result, substitution);
            }
        }
        return result;
    }

    private static TriggerCondition transformStateTrigger(TriggerCondition condition, StateTrigger state,
                                                          List<TextReplacement> replacements) {
        PermanentPredicate source = state.sourcePredicate();
        PermanentPredicate battlefield = state.battlefieldPredicate();
        PermanentPredicate referenced = state.referencedPermanentPredicate();
        for (TextReplacement replacement : replacements) {
            Substitution substitution = resolve(replacement);
            if (substitution != null) {
                source = apply(source, substitution);
                battlefield = apply(battlefield, substitution);
                referenced = apply(referenced, substitution);
            }
        }
        if (source == state.sourcePredicate() && battlefield == state.battlefieldPredicate()
                && referenced == state.referencedPermanentPredicate()) {
            return condition;
        }
        return new TriggerCondition(new StateTrigger(state.predicate(), source, battlefield,
                state.maximumBattlefieldMatches(), state.minimumBattlefieldMatches(), referenced), condition.zones());
    }

    /**
     * The object's own replacements, then the global ones; a timestamped own change is newer than the global rewrite
     * only if it was made later, so then the two lists are applied together in timestamp order (CR 613.7). The sort is
     * stable, so untimed own replacements stay ahead of the global ones.
     */
    private static List<TextReplacement> inOrder(List<TextReplacement> own, List<TextReplacement> global) {
        if (global.isEmpty()) {
            return own;
        }
        List<TextReplacement> inOrder = new ArrayList<>(own);
        inOrder.addAll(global);
        if (own.stream().anyMatch(replacement -> replacement.timestamp() > 0)) {
            inOrder.sort(Comparator.comparingLong(TextReplacement::timestamp));
        }
        return inOrder;
    }

    /**
     * Applies the given text replacements to the effect in order, returning the rewritten
     * effect — or the same instance when no replacement changes it.
     */
    public static CardEffect transform(CardEffect effect, List<TextReplacement> replacements) {
        CardEffect result = effect;
        for (TextReplacement replacement : replacements) {
            Substitution substitution = resolve(replacement);
            if (substitution != null) {
                result = apply(result, substitution);
            }
        }
        return result;
    }

    /** Rewrites color words in a spell's permanent target restriction. */
    public static TargetFilter transformTargetFilter(TargetFilter filter, List<TextReplacement> replacements) {
        if (!(filter instanceof PermanentPredicateTargetFilter permanentFilter)) {
            return filter;
        }
        PermanentPredicate predicate = permanentFilter.predicate();
        for (TextReplacement replacement : replacements) {
            Substitution substitution = resolve(replacement);
            if (substitution != null) {
                predicate = apply(predicate, substitution);
            }
        }
        return predicate == permanentFilter.predicate() ? filter
                : new PermanentPredicateTargetFilter(predicate, permanentFilter.errorMessage());
    }

    public static Set<Keyword> transformKeywords(Set<Keyword> keywords, List<TextReplacement> replacements) {
        Set<Keyword> result = keywords;
        for (TextReplacement replacement : replacements) {
            Substitution substitution = resolve(replacement);
            if (substitution != null) {
                result = replaceLandwalk(result, substitution);
            }
        }
        return result;
    }

    /** The basic land type a text-change word denotes, or {@code null} for color words. */
    public static CardSubtype basicLandTypeForWord(String word) {
        return BASIC_LAND_WORDS.get(word);
    }

    /** The creature type denoted by a text-change word, or null for other words. */
    public static CardSubtype creatureTypeForWord(String word) {
        return CREATURE_TYPE_WORDS.get(word);
    }

    private static Substitution resolve(TextReplacement replacement) {
        CardColor fromColor = COLOR_WORDS.get(replacement.fromWord());
        CardColor toColor = COLOR_WORDS.get(replacement.toWord());
        if (fromColor != null && toColor != null) {
            return new Substitution(fromColor, toColor, null, null, null, null);
        }
        CardSubtype fromLandType = BASIC_LAND_WORDS.get(replacement.fromWord());
        CardSubtype toLandType = BASIC_LAND_WORDS.get(replacement.toWord());
        if (fromLandType != null && toLandType != null) {
            return new Substitution(null, null, fromLandType, toLandType, null, null);
        }
        CardSubtype fromCreatureType = CREATURE_TYPE_WORDS.get(replacement.fromWord());
        CardSubtype toCreatureType = CREATURE_TYPE_WORDS.get(replacement.toWord());
        if (fromCreatureType != null && toCreatureType != null) {
            return new Substitution(null, null, null, null, fromCreatureType, toCreatureType);
        }
        return null;
    }

    private static CardEffect apply(CardEffect effect, Substitution substitution) {
        if (effect == null) {
            // Single-branch conditional wrappers carry a null inactive branch.
            return null;
        }
        return switch (effect) {
            case ExileAllPermanentsEffect exile -> {
                PermanentPredicate filter = apply(exile.filter(), substitution);
                yield filter == exile.filter() ? exile : new ExileAllPermanentsEffect(filter,
                        exile.trackWithSource(), exile.returnOneAtEachUpkeep(), exile.ownerMayPlayWhileExiled(),
                        exile.perpetualCastCostIncrease(), exile.perpetualEnterTapped(),
                        exile.controllerMayPlayWhileExiled(), exile.controllerMaySpendAnyManaType());
            }
            // "Destroy all Islands", "exile all Swamps": the set's words.
            case DestroyTargetPermanentEffect destroy when destroy.subject().isSet() -> {
                PermanentSubject subject = apply(destroy.subject(), substitution);
                yield subject == destroy.subject() ? destroy
                        : new DestroyTargetPermanentEffect(destroy.cannotBeRegenerated(), destroy.tokenForController(),
                        destroy.targetGroup(), destroy.targetFilter(), destroy.tokenOnlyForSourceController(),
                        destroy.tokenCount(), destroy.targetChosenByActivePlayer(), subject,
                        destroy.referencedTarget());
            }
            case ExileTargetPermanentEffect exile when exile.subject().isSet() -> {
                PermanentSubject subject = apply(exile.subject(), substitution);
                yield subject == exile.subject() ? exile
                        : new ExileTargetPermanentEffect(exile.tokenForController(), exile.targetFilter(), subject,
                        exile.outsideGame());
            }
            case DestroyAllPermanentsEffect destroy -> {
                PermanentPredicate filter = apply(destroy.filter(), substitution);
                CardEffect thenEffect = apply(destroy.thenEffect(), substitution);
                yield filter == destroy.filter() && thenEffect == destroy.thenEffect() ? destroy
                        : new DestroyAllPermanentsEffect(filter, destroy.cannotBeRegenerated(), thenEffect);
            }
            case ChangeColorTextEffect change -> {
                CardSubtype excluded = replaceSubtype(change.excludedReplacementCreatureType(), substitution);
                yield excluded == change.excludedReplacementCreatureType() ? change
                        : new ChangeColorTextEffect(change.colorWordsAllowed(), change.landTypesAllowed(),
                        change.canTargetSpell(), change.untilEndOfTurn(), change.creatureTypesAllowed(), excluded,
                        change.fixedReplacementCreatureType());
            }
            case ReturnToHandEffect bounce -> {
                PermanentPredicate filter = apply(bounce.filter(), substitution);
                yield filter == bounce.filter() ? bounce : bounce.withFilter(filter);
            }
            // Every word of a boost (CR 612.2): its amounts ("for each Forest you control"), its target filter ("target
            // Pirate"), the set it boosts ("white creatures get") and the landwalk it grants.
            case BoostEffect boost -> {
                DynamicAmount power = apply(boost.powerBoost(), substitution);
                DynamicAmount toughness = apply(boost.toughnessBoost(), substitution);
                PermanentPredicate filter = apply(boost.filter(), substitution);
                DynamicAmount castTimeXValue = apply(boost.castTimeXValue(), substitution);
                PermanentSubject subject = apply(boost.subject(), substitution);
                Set<Keyword> keywords = replaceLandwalk(boost.keywords(), substitution);
                yield power == boost.powerBoost() && toughness == boost.toughnessBoost()
                        && filter == boost.filter() && castTimeXValue == boost.castTimeXValue()
                        && subject == boost.subject() && keywords == boost.keywords() ? boost
                        : new BoostEffect(power, toughness, subject, boost.amountsRead(), boost.duration(), keywords,
                        filter, boost.targetsPermanent(), boost.targetGroup(), castTimeXValue);
            }
            case com.github.laxika.magicalvibes.model.effect.GrantProtectionFromColorUntilEndOfTurnEffect protection -> {
                CardColor color = protection.color() == substitution.fromColor()
                        ? substitution.toColor() : protection.color();
                PermanentPredicate predicate = apply(protection.predicate(), substitution);
                yield color == protection.color() && predicate == protection.predicate() ? protection
                        : new com.github.laxika.magicalvibes.model.effect.GrantProtectionFromColorUntilEndOfTurnEffect(
                                color, predicate, protection.subject(), protection.declaredTarget());
            }
            case ProtectionFromColorsEffect protection -> {
                Set<CardColor> colors = replaceColor(protection.colors(), substitution);
                yield colors == protection.colors() ? protection : protection.withColors(colors);
            }
            case PreventDamageFromChosenSourceEffect prevention -> {
                PermanentPredicate filter = apply(prevention.sourceFilter(), substitution);
                String label = replaceColorWord(prevention.sourceLabel(), substitution);
                yield filter == prevention.sourceFilter() && label == prevention.sourceLabel() ? prevention
                        : new PreventDamageFromChosenSourceEffect(
                                prevention.scope(), prevention.gainLife(),
                                prevention.gainLifeForBlackOrRedSource(), prevention.controllerOnly(),
                                filter, label, prevention.sourceChosenColor(),
                                prevention.sourceSharesColorWithImprintedCard(),
                                prevention.sourceActivationManaColor(), prevention.exileFromLibrary(),
                                prevention.damageRedSourceController(), prevention.damageSourceController(),
                                prevention.preventHalfDamage(), prevention.drawCards());
            }
            case GrantColorEffect grant -> {
                CardColor color = substitution.fromColor() != null && grant.color() == substitution.fromColor()
                        ? substitution.toColor() : grant.color();
                PermanentPredicate filter = apply(grant.filter(), substitution);
                yield color == grant.color() && filter == grant.filter() ? grant
                        : new GrantColorEffect(color, grant.subject(), grant.overriding(), filter, grant.ofChosenColor());
            }
            case EnchantedPermanentBecomesTypeEffect becomes -> {
                if (substitution.fromLandType() == null) {
                    yield becomes;
                }
                List<CardSubtype> updated = becomes.subtypes().stream()
                        .map(s -> s == substitution.fromLandType() ? substitution.toLandType() : s)
                        .toList();
                yield updated.equals(becomes.subtypes()) ? becomes
                        : new EnchantedPermanentBecomesTypeEffect(updated);
            }
            // "All Forests are 1/1 creatures" (Living Lands), "All Swamps are 1/1 black creatures" (Kormus Bell).
            case AllLandsAreCreaturesEffect animate -> {
                CardSubtype subtype = replaceSubtype(animate.requiredSubtype(), substitution);
                CardColor color = substitution.fromColor() != null && animate.color() == substitution.fromColor()
                        ? substitution.toColor() : animate.color();
                yield subtype == animate.requiredSubtype() && color == animate.color() ? animate
                        : new AllLandsAreCreaturesEffect(animate.power(), animate.toughness(), subtype, color,
                        animate.subject());
            }
            case NonbasicLandsBecomeTypeEffect becomes ->
                    substitution.fromLandType() != null && becomes.subtype() == substitution.fromLandType()
                            ? new NonbasicLandsBecomeTypeEffect(substitution.toLandType())
                            : becomes;
            case GrantSubtypeEffect grant -> {
                CardSubtype subtype = replaceSubtype(grant.subtype(), substitution);
                PermanentPredicate filter = apply(grant.filter(), substitution);
                yield subtype == grant.subtype() && filter == grant.filter() ? grant
                        : new GrantSubtypeEffect(subtype, grant.subject(), grant.overriding(), filter, grant.chosen());
            }
            case StaticBoostEffect boost -> {
                Set<Keyword> keywords = replaceLandwalk(boost.grantedKeywords(), substitution);
                PermanentPredicate filter = apply(boost.filter(), substitution);
                yield keywords == boost.grantedKeywords() && filter == boost.filter() ? boost
                        : new StaticBoostEffect(boost.powerBoost(), boost.toughnessBoost(),
                        keywords, boost.subject(), filter, boost.scalingCounter(), boost.scalingCounterOnTarget());
            }
            case DynamicStaticBoostEffect boost -> {
                PermanentPredicate filter = apply(boost.filter(), substitution);
                yield filter == boost.filter() ? boost
                        : new DynamicStaticBoostEffect(boost.powerBoost(), boost.toughnessBoost(),
                        boost.subject(), filter, boost.amountsFromTarget());
            }
            case GrantKeywordEffect grant -> {
                Set<Keyword> keywords = replaceLandwalk(grant.keywords(), substitution);
                PermanentPredicate filter = apply(grant.filter(), substitution);
                PermanentPredicate condition = apply(grant.grantCondition(), substitution);
                yield keywords == grant.keywords() && filter == grant.filter() && condition == grant.grantCondition()
                        ? grant
                        : new GrantKeywordEffect(keywords, grant.subject(), filter, grant.duration(), condition);
            }
            case GrantNonAbilityEffectEffect grant -> {
                CardEffect wrapped = apply(grant.effect(), substitution);
                PermanentPredicate filter = apply(grant.filter(), substitution);
                yield wrapped == grant.effect() && filter == grant.filter() ? grant
                        : new GrantNonAbilityEffectEffect(wrapped, grant.subject(), filter);
            }
            case GrantEffectEffect grant -> {
                CardEffect wrapped = apply(grant.effect(), substitution);
                PermanentPredicate filter = apply(grant.filter(), substitution);
                yield wrapped == grant.effect() && filter == grant.filter() ? grant
                        : new GrantEffectEffect(wrapped, grant.subject(), filter);
            }
            case ConditionalEffect conditional -> {
                Condition condition = apply(conditional.condition(), substitution);
                CardEffect wrapped = apply(conditional.wrapped(), substitution);
                yield condition == conditional.condition() && wrapped == conditional.wrapped() ? conditional
                        : new ConditionalEffect(condition, wrapped);
            }
            // "Can't be countered by blue or black spells" (Autumn's Veil).
            case GrantControllerSpellsCantBeCounteredByColorsEffect cantBeCountered -> {
                Set<CardColor> colors = replaceColor(cantBeCountered.colors(), substitution);
                yield colors == cantBeCountered.colors() ? cantBeCountered
                        : new GrantControllerSpellsCantBeCounteredByColorsEffect(colors);
            }
            // "Can't be the targets of blue or black spells this turn" (Autumn's Veil).
            case GrantTargetingRestrictionToTargetUntilEndOfTurnEffect grant -> {
                TargetingRestrictionEffect restriction = grant.restriction();
                Set<CardColor> colors = restriction.colors() == null ? null
                        : replaceColor(restriction.colors(), substitution);
                yield colors == restriction.colors() ? grant
                        : new GrantTargetingRestrictionToTargetUntilEndOfTurnEffect(new TargetingRestrictionEffect(
                        restriction.kind(), restriction.opponentOnly(), colors, restriction.mode(),
                        restriction.hexproofLike(), restriction.sourceCardTypes(), restriction.opponentsOf()),
                        grant.subject());
            }
            // Several steps: the words of each step.
            case SequenceEffect sequence -> {
                List<CardEffect> steps = new ArrayList<>(sequence.steps().size());
                boolean changed = false;
                for (CardEffect step : sequence.steps()) {
                    CardEffect rewritten = apply(step, substitution);
                    changed |= rewritten != step;
                    steps.add(rewritten);
                }
                yield changed ? sequence.withSteps(steps) : sequence;
            }
            // A "you may": the words of the effect it wraps.
            case MayEffect may -> {
                CardEffect wrapped = apply(may.wrapped(), substitution);
                yield wrapped == may.wrapped() ? may
                        : new MayEffect(wrapped, may.prompt(), may.elseEffect(), may.choicePlayer(), may.choicePlayerId(),
                            may.targetOfTheirChoice());
            }
            case EnchantedPermanentConditionalEffect conditional -> {
                PermanentPredicate filter = apply(conditional.filter(), substitution);
                CardEffect ifMatch = apply(conditional.ifMatch(), substitution);
                CardEffect ifNotMatch = apply(conditional.ifNotMatch(), substitution);
                yield filter == conditional.filter() && ifMatch == conditional.ifMatch()
                        && ifNotMatch == conditional.ifNotMatch() ? conditional
                        : new EnchantedPermanentConditionalEffect(filter, ifMatch, ifNotMatch);
            }
            case LandwalkIgnoredForBlockingEffect ignored -> {
                if (substitution.fromLandType() == null || ignored.landwalkKeyword() == null) {
                    yield ignored;
                }
                Keyword fromWalk = LANDWALK_BY_LAND_TYPE.get(substitution.fromLandType());
                Keyword toWalk = LANDWALK_BY_LAND_TYPE.get(substitution.toLandType());
                yield fromWalk == null || toWalk == null || ignored.landwalkKeyword() != fromWalk
                        ? ignored
                        : new LandwalkIgnoredForBlockingEffect(toWalk);
            }
            default -> effect;
        };
    }

    /** A count's permanent filter with its words changed, through the amounts that scale or gate one. */
    private static DynamicAmount apply(DynamicAmount amount, Substitution substitution) {
        return switch (amount) {
            case PermanentCount count -> {
                PermanentPredicate filter = apply(count.filter(), substitution);
                yield filter == count.filter() ? count : new PermanentCount(filter, count.scope(),
                        count.excludeSource(), count.declaredAttackersOnly());
            }
            case Scaled scaled -> {
                DynamicAmount inner = apply(scaled.amount(), substitution);
                yield inner == scaled.amount() ? scaled : new Scaled(inner, scaled.factor());
            }
            case Divided divided -> {
                DynamicAmount inner = apply(divided.amount(), substitution);
                yield inner == divided.amount() ? divided : new Divided(inner, divided.divisor());
            }
            case HalvedRoundedUp halved -> {
                DynamicAmount inner = apply(halved.amount(), substitution);
                yield inner == halved.amount() ? halved : new HalvedRoundedUp(inner);
            }
            case DeterminedOnce once -> {
                DynamicAmount inner = apply(once.amount(), substitution);
                yield inner == once.amount() ? once : new DeterminedOnce(inner);
            }
            case DuringControllerTurn duringTurn -> {
                DynamicAmount inner = apply(duringTurn.amount(), substitution);
                yield inner == duringTurn.amount() ? duringTurn : new DuringControllerTurn(inner);
            }
            case IfSourceAttacking ifAttacking -> {
                DynamicAmount whileAttacking = apply(ifAttacking.whileAttacking(), substitution);
                DynamicAmount otherwise = apply(ifAttacking.otherwise(), substitution);
                yield whileAttacking == ifAttacking.whileAttacking() && otherwise == ifAttacking.otherwise()
                        ? ifAttacking : new IfSourceAttacking(whileAttacking, otherwise);
            }
            case null, default -> amount;
        };
    }

    private static Condition apply(Condition condition, Substitution substitution) {
        return switch (condition) {
            case TargetPermanentMatches matches -> {
                PermanentPredicate filter = apply(matches.filter(), substitution);
                yield filter == matches.filter() ? matches : new TargetPermanentMatches(filter);
            }
            case TargetSpellMatches matches -> {
                StackEntryPredicate filter = apply(matches.filter(), substitution);
                yield filter == matches.filter() ? matches : new TargetSpellMatches(filter);
            }
            case AllOf allOf -> {
                List<Condition> conditions = applyConditions(allOf.conditions(), substitution);
                yield conditions == allOf.conditions() ? allOf : new AllOf(conditions);
            }
            case AnyOf anyOf -> {
                List<Condition> conditions = applyConditions(anyOf.conditions(), substitution);
                yield conditions == anyOf.conditions() ? anyOf : new AnyOf(conditions);
            }
            case NotCondition not -> {
                Condition inner = apply(not.inner(), substitution);
                yield inner == not.inner() ? not : new NotCondition(inner);
            }
            // "If you control two or more Islands" (Noggle Hedge-Mage), "if that player controls a nonblack, nonland
            // permanent" (Urborg Stalker): the words of the counted permanents.
            case ControlsPermanentCount count -> {
                PermanentPredicate filter = apply(count.filter(), substitution);
                yield filter == count.filter() ? count : new ControlsPermanentCount(count.minCount(), filter);
            }
            case ActivePlayerControlsPermanent controls -> {
                PermanentPredicate filter = apply(controls.filter(), substitution);
                yield filter == controls.filter() ? controls : new ActivePlayerControlsPermanent(filter);
            }
            // "If you sacrifice an Island this way": the words of the permanent and card filters.
            case HappenedThisWay happened -> {
                PermanentPredicate filter = apply(happened.filter(), substitution);
                CardPredicate cardFilter = apply(happened.cardFilter(), substitution);
                yield filter == happened.filter() && cardFilter == happened.cardFilter() ? happened
                        : new HappenedThisWay(happened.kind(), happened.minimum(), filter, cardFilter, happened.player());
            }
            default -> condition;
        };
    }

    private static List<Condition> applyConditions(List<Condition> conditions, Substitution substitution) {
        List<Condition> result = new ArrayList<>(conditions.size());
        boolean changed = false;
        for (Condition condition : conditions) {
            Condition transformed = apply(condition, substitution);
            changed |= transformed != condition;
            result.add(transformed);
        }
        return changed ? result : conditions;
    }

    private static CardPredicate apply(CardPredicate predicate, Substitution substitution) {
        return switch (predicate) {
            case null -> null;
            case CardColorPredicate color -> substitution.fromColor() != null
                    && color.color() == substitution.fromColor()
                    ? new CardColorPredicate(substitution.toColor()) : color;
            case CardSubtypePredicate has -> {
                CardSubtype subtype = replaceSubtype(has.subtype(), substitution);
                yield subtype == has.subtype() ? has : new CardSubtypePredicate(subtype);
            }
            case CardAllOfPredicate allOf -> {
                List<CardPredicate> children = applyCardPredicates(allOf.predicates(), substitution);
                yield children == allOf.predicates() ? allOf : new CardAllOfPredicate(children);
            }
            case CardAnyOfPredicate anyOf -> {
                List<CardPredicate> children = applyCardPredicates(anyOf.predicates(), substitution);
                yield children == anyOf.predicates() ? anyOf : new CardAnyOfPredicate(children);
            }
            case CardNotPredicate not -> {
                CardPredicate inner = apply(not.predicate(), substitution);
                yield inner == not.predicate() ? not : new CardNotPredicate(inner);
            }
            default -> predicate;
        };
    }

    private static List<CardPredicate> applyCardPredicates(List<CardPredicate> predicates,
                                                           Substitution substitution) {
        List<CardPredicate> result = new ArrayList<>(predicates.size());
        boolean changed = false;
        for (CardPredicate predicate : predicates) {
            CardPredicate transformed = apply(predicate, substitution);
            changed |= transformed != predicate;
            result.add(transformed);
        }
        return changed ? result : predicates;
    }

    private static StackEntryPredicate apply(StackEntryPredicate predicate, Substitution substitution) {
        return switch (predicate) {
            case StackEntryColorInPredicate colorIn -> {
                Set<CardColor> colors = replaceColor(colorIn.colors(), substitution);
                yield colors == colorIn.colors() ? colorIn : new StackEntryColorInPredicate(colors);
            }
            case StackEntryAllOfPredicate allOf -> {
                List<StackEntryPredicate> children = applyStackPredicates(allOf.predicates(), substitution);
                yield children == allOf.predicates() ? allOf : new StackEntryAllOfPredicate(children);
            }
            case StackEntryAnyOfPredicate anyOf -> {
                List<StackEntryPredicate> children = applyStackPredicates(anyOf.predicates(), substitution);
                yield children == anyOf.predicates() ? anyOf : new StackEntryAnyOfPredicate(children);
            }
            case StackEntryNotPredicate not -> {
                StackEntryPredicate inner = apply(not.predicate(), substitution);
                yield inner == not.predicate() ? not : new StackEntryNotPredicate(inner);
            }
            default -> predicate;
        };
    }

    private static List<StackEntryPredicate> applyStackPredicates(List<StackEntryPredicate> predicates,
                                                                   Substitution substitution) {
        List<StackEntryPredicate> result = new ArrayList<>(predicates.size());
        boolean changed = false;
        for (StackEntryPredicate predicate : predicates) {
            StackEntryPredicate transformed = apply(predicate, substitution);
            changed |= transformed != predicate;
            result.add(transformed);
        }
        return changed ? result : predicates;
    }

    /** A set subject with its filter's words changed; any other subject is returned as it is. */
    private static PermanentSubject apply(PermanentSubject subject, Substitution substitution) {
        return switch (subject) {
            case PermanentSubject.ControlledBy set -> {
                PermanentPredicate filter = apply(set.filter(), substitution);
                yield filter == set.filter() ? set : Subjects.controlledBy(set.players(), filter);
            }
            case PermanentSubject.OwnedBy set -> {
                PermanentPredicate filter = apply(set.filter(), substitution);
                yield filter == set.filter() ? set : new PermanentSubject.OwnedBy(set.players(), filter);
            }
            case PermanentSubject.Except except -> {
                PermanentSubject permanents = apply(except.permanents(), substitution);
                yield permanents == except.permanents() ? except : Subjects.except(permanents, except.excluded());
            }
            default -> subject;
        };
    }

    private static PermanentPredicate apply(PermanentPredicate predicate, Substitution substitution) {
        return switch (predicate) {
            case null -> null;
            case PermanentHasSubtypePredicate has ->
                    replaceSubtype(has.subtype(), substitution) == has.subtype()
                            ? has : new PermanentHasSubtypePredicate(replaceSubtype(has.subtype(), substitution));
            case PermanentHasAnySubtypePredicate has -> {
                CardSubtype fromSubtype = fromSubtype(substitution);
                CardSubtype toSubtype = toSubtype(substitution);
                if (fromSubtype == null || !has.subtypes().contains(fromSubtype)) {
                    yield has;
                }
                Set<CardSubtype> subtypes = EnumSet.copyOf(has.subtypes());
                subtypes.remove(fromSubtype);
                subtypes.add(toSubtype);
                yield new PermanentHasAnySubtypePredicate(subtypes);
            }
            case PermanentColorInPredicate colorIn -> {
                Set<CardColor> colors = replaceColor(colorIn.colors(), substitution);
                yield colors == colorIn.colors() ? colorIn : new PermanentColorInPredicate(colors);
            }
            case PermanentPowerAtMostControlledSubtypeCountPredicate count ->
                    replaceSubtype(count.subtype(), substitution) == count.subtype()
                            ? count : new PermanentPowerAtMostControlledSubtypeCountPredicate(
                                    replaceSubtype(count.subtype(), substitution));
            case PermanentAllOfPredicate allOf -> {
                List<PermanentPredicate> children = applyAll(allOf.predicates(), substitution);
                yield children == allOf.predicates() ? allOf : new PermanentAllOfPredicate(children);
            }
            case PermanentAnyOfPredicate anyOf -> {
                List<PermanentPredicate> children = applyAll(anyOf.predicates(), substitution);
                yield children == anyOf.predicates() ? anyOf : new PermanentAnyOfPredicate(children);
            }
            case PermanentNotPredicate not -> {
                PermanentPredicate inner = apply(not.predicate(), substitution);
                yield inner == not.predicate() ? not : new PermanentNotPredicate(inner);
            }
            default -> predicate;
        };
    }

    private static List<PermanentPredicate> applyAll(List<PermanentPredicate> predicates, Substitution substitution) {
        List<PermanentPredicate> result = new ArrayList<>(predicates.size());
        boolean changed = false;
        for (PermanentPredicate predicate : predicates) {
            PermanentPredicate transformed = apply(predicate, substitution);
            changed |= transformed != predicate;
            result.add(transformed);
        }
        return changed ? result : predicates;
    }

    private static Set<CardColor> replaceColor(Set<CardColor> colors, Substitution substitution) {
        if (substitution.fromColor() == null || !colors.contains(substitution.fromColor())) {
            return colors;
        }
        Set<CardColor> result = EnumSet.copyOf(colors);
        result.remove(substitution.fromColor());
        result.add(substitution.toColor());
        return result;
    }

    private static String replaceColorWord(String word, Substitution substitution) {
        if (word == null || substitution.fromColor() == null
                || !word.equals(substitution.fromColor().name().toLowerCase(Locale.ROOT))) {
            return word;
        }
        return substitution.toColor().name().toLowerCase(Locale.ROOT);
    }

    private static Set<Keyword> replaceLandwalk(Set<Keyword> keywords, Substitution substitution) {
        if (substitution.fromLandType() == null) {
            return keywords;
        }
        Keyword fromWalk = LANDWALK_BY_LAND_TYPE.get(substitution.fromLandType());
        Keyword toWalk = LANDWALK_BY_LAND_TYPE.get(substitution.toLandType());
        if (fromWalk == null || toWalk == null || !keywords.contains(fromWalk)) {
            return keywords;
        }
        Set<Keyword> result = new HashSet<>(keywords);
        result.remove(fromWalk);
        result.add(toWalk);
        return result;
    }

    private static CardSubtype replaceSubtype(CardSubtype subtype, Substitution substitution) {
        return subtype == fromSubtype(substitution) ? toSubtype(substitution) : subtype;
    }

    private static CardSubtype fromSubtype(Substitution substitution) {
        return substitution.fromLandType() != null ? substitution.fromLandType() : substitution.fromCreatureType();
    }

    private static CardSubtype toSubtype(Substitution substitution) {
        return substitution.toLandType() != null ? substitution.toLandType() : substitution.toCreatureType();
    }
}
