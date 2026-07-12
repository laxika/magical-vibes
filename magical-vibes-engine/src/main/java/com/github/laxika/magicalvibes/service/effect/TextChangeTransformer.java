package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.NonbasicLandsBecomeTypeEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Layer 3 (CR 613.2c / 612): text-changing effects rewrite the WORDS of an object's abilities,
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
 * the color/land-type parameters of one-shot spell effects are never re-read from a permanent's
 * text after resolution, so they need no rewriting here. Chosen-as-enters colors are stored on
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
                                CardSubtype fromLandType, CardSubtype toLandType) {
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

    /** The basic land type a text-change word denotes, or {@code null} for color words. */
    public static CardSubtype basicLandTypeForWord(String word) {
        return BASIC_LAND_WORDS.get(word);
    }

    private static Substitution resolve(TextReplacement replacement) {
        CardColor fromColor = COLOR_WORDS.get(replacement.fromWord());
        CardColor toColor = COLOR_WORDS.get(replacement.toWord());
        if (fromColor != null && toColor != null) {
            return new Substitution(fromColor, toColor, null, null);
        }
        CardSubtype fromLandType = BASIC_LAND_WORDS.get(replacement.fromWord());
        CardSubtype toLandType = BASIC_LAND_WORDS.get(replacement.toWord());
        if (fromLandType != null && toLandType != null) {
            return new Substitution(null, null, fromLandType, toLandType);
        }
        return null;
    }

    private static CardEffect apply(CardEffect effect, Substitution substitution) {
        if (effect == null) {
            // Single-branch conditional wrappers carry a null inactive branch.
            return null;
        }
        return switch (effect) {
            case ProtectionFromColorsEffect protection -> {
                Set<CardColor> colors = replaceColor(protection.colors(), substitution);
                yield colors == protection.colors() ? protection
                        : new ProtectionFromColorsEffect(colors, protection.scope());
            }
            case GrantColorEffect grant ->
                    substitution.fromColor() != null && grant.color() == substitution.fromColor()
                            ? new GrantColorEffect(substitution.toColor(), grant.scope(), grant.overriding())
                            : grant;
            case EnchantedPermanentBecomesTypeEffect becomes ->
                    substitution.fromLandType() != null && becomes.subtype() == substitution.fromLandType()
                            ? new EnchantedPermanentBecomesTypeEffect(substitution.toLandType())
                            : becomes;
            case NonbasicLandsBecomeTypeEffect becomes ->
                    substitution.fromLandType() != null && becomes.subtype() == substitution.fromLandType()
                            ? new NonbasicLandsBecomeTypeEffect(substitution.toLandType())
                            : becomes;
            case GrantSubtypeEffect grant -> {
                CardSubtype subtype = substitution.fromLandType() != null && grant.subtype() == substitution.fromLandType()
                        ? substitution.toLandType() : grant.subtype();
                PermanentPredicate filter = apply(grant.filter(), substitution);
                yield subtype == grant.subtype() && filter == grant.filter() ? grant
                        : new GrantSubtypeEffect(subtype, grant.scope(), grant.overriding(), filter);
            }
            case StaticBoostEffect boost -> {
                Set<Keyword> keywords = replaceLandwalk(boost.grantedKeywords(), substitution);
                PermanentPredicate filter = apply(boost.filter(), substitution);
                yield keywords == boost.grantedKeywords() && filter == boost.filter() ? boost
                        : new StaticBoostEffect(boost.powerBoost(), boost.toughnessBoost(),
                        keywords, boost.scope(), filter);
            }
            case GrantKeywordEffect grant -> {
                Set<Keyword> keywords = replaceLandwalk(grant.keywords(), substitution);
                PermanentPredicate filter = apply(grant.filter(), substitution);
                PermanentPredicate condition = apply(grant.grantCondition(), substitution);
                yield keywords == grant.keywords() && filter == grant.filter() && condition == grant.grantCondition()
                        ? grant
                        : new GrantKeywordEffect(keywords, grant.scope(), filter, grant.duration(), condition);
            }
            case GrantEffectEffect grant -> {
                CardEffect wrapped = apply(grant.effect(), substitution);
                PermanentPredicate filter = apply(grant.filter(), substitution);
                yield wrapped == grant.effect() && filter == grant.filter() ? grant
                        : new GrantEffectEffect(wrapped, grant.scope(), filter);
            }
            case ConditionalEffect conditional -> {
                CardEffect wrapped = apply(conditional.wrapped(), substitution);
                yield wrapped == conditional.wrapped() ? conditional
                        : new ConditionalEffect(conditional.condition(), wrapped);
            }
            case EnchantedPermanentConditionalEffect conditional -> {
                PermanentPredicate filter = apply(conditional.filter(), substitution);
                CardEffect ifMatch = apply(conditional.ifMatch(), substitution);
                CardEffect ifNotMatch = apply(conditional.ifNotMatch(), substitution);
                yield filter == conditional.filter() && ifMatch == conditional.ifMatch()
                        && ifNotMatch == conditional.ifNotMatch() ? conditional
                        : new EnchantedPermanentConditionalEffect(filter, ifMatch, ifNotMatch);
            }
            default -> effect;
        };
    }

    private static PermanentPredicate apply(PermanentPredicate predicate, Substitution substitution) {
        return switch (predicate) {
            case null -> null;
            case PermanentHasSubtypePredicate has ->
                    substitution.fromLandType() != null && has.subtype() == substitution.fromLandType()
                            ? new PermanentHasSubtypePredicate(substitution.toLandType())
                            : has;
            case PermanentHasAnySubtypePredicate has -> {
                if (substitution.fromLandType() == null || !has.subtypes().contains(substitution.fromLandType())) {
                    yield has;
                }
                Set<CardSubtype> subtypes = EnumSet.copyOf(has.subtypes());
                subtypes.remove(substitution.fromLandType());
                subtypes.add(substitution.toLandType());
                yield new PermanentHasAnySubtypePredicate(subtypes);
            }
            case PermanentColorInPredicate colorIn -> {
                Set<CardColor> colors = replaceColor(colorIn.colors(), substitution);
                yield colors == colorIn.colors() ? colorIn : new PermanentColorInPredicate(colors);
            }
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
}
