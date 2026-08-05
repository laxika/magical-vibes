package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTruePredicate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factories for {@link TargetPredicate}: the five kind leaves, the cross-kind disjunction, and one
 * named factory per {@link TargetCategory} constant so the migration off the enum is mechanical.
 *
 * <p>The two categories the enum could not tell apart become structurally distinct here:
 * {@link #playerOrPermanent()} is "a player or <em>any</em> permanent" while {@link #anyTarget()}
 * is CR 115.4's "any target" — a creature, planeswalker or player. Both were
 * {@code (includesPermanents=true, includesPlayers=true)} before.</p>
 */
public final class TargetPredicates {

    /** No restriction within a kind — spelled out so no leaf ever carries a {@code null} inner. */
    private static final PermanentPredicate ANY_PERMANENT = new PermanentTruePredicate();
    private static final PlayerPredicate ANY_PLAYER = new PlayerRelationPredicate(PlayerRelation.ANY);
    private static final CardPredicate ANY_CARD = new CardTruePredicate();
    private static final StackEntryPredicate ANY_SPELL = new StackEntryTruePredicate();

    private static final PermanentPredicate IS_CREATURE = new PermanentIsCreaturePredicate();
    private static final PermanentPredicate IS_LAND = new PermanentIsLandPredicate();
    private static final PermanentPredicate IS_PLANESWALKER = new PermanentIsPlaneswalkerPredicate();
    private static final PermanentPredicate IS_CREATURE_OR_PLANESWALKER =
            new PermanentAnyOfPredicate(List.of(IS_CREATURE, IS_PLANESWALKER));

    /**
     * The fourteen canonical declared targets, interned. {@code TargetSpec} values are rebuilt on
     * every {@code CardEffect.targetSpec()} call and target enumeration calls that in a loop, so the
     * named factories hand back a shared immutable value rather than allocating one per call.
     */
    private static final TargetPredicate PLAYER = players(ANY_PLAYER);
    private static final TargetPredicate PERMANENT = permanents(ANY_PERMANENT);
    private static final TargetPredicate CREATURE = permanents(IS_CREATURE);
    private static final TargetPredicate LAND = permanents(IS_LAND);
    private static final TargetPredicate CREATURE_OR_PLANESWALKER = permanents(IS_CREATURE_OR_PLANESWALKER);
    private static final TargetPredicate PLAYER_OR_PERMANENT = anyOf(PLAYER, PERMANENT);
    private static final TargetPredicate PLAYER_OR_PLANESWALKER = anyOf(PLAYER, permanents(IS_PLANESWALKER));
    private static final TargetPredicate ANY_TARGET = anyOf(PLAYER, CREATURE_OR_PLANESWALKER);
    private static final TargetPredicate SPELL_ON_STACK = spells(ANY_SPELL);
    private static final TargetPredicate GRAVEYARD_CARD =
            graveyardCards(ANY_CARD, GraveyardSearchScope.OPPONENT_GRAVEYARD);
    private static final TargetPredicate ANY_GRAVEYARD_CARD =
            graveyardCards(ANY_CARD, GraveyardSearchScope.ALL_GRAVEYARDS);
    private static final TargetPredicate CONTROLLERS_GRAVEYARD_CARD =
            graveyardCards(ANY_CARD, GraveyardSearchScope.CONTROLLERS_GRAVEYARD);
    private static final TargetPredicate EXILE_CARD = exiledCards(ANY_CARD);

    /** Inverse of {@link #forCategory(TargetCategory)}; see {@link #categoryOf(TargetPredicate)}. */
    private static final Map<TargetPredicate, TargetCategory> CATEGORY_BY_TARGET = buildCategoryIndex();

    private TargetPredicates() {
    }

    /** A battlefield permanent matching {@code predicate}. */
    public static TargetPredicate permanents(PermanentPredicate predicate) {
        return new TargetPredicate.Permanents(predicate);
    }

    /** A player matching {@code predicate}. */
    public static TargetPredicate players(PlayerPredicate predicate) {
        return new TargetPredicate.Players(predicate);
    }

    /** A card in a graveyard within {@code scope} matching {@code predicate}. */
    public static TargetPredicate graveyardCards(CardPredicate predicate, GraveyardSearchScope scope) {
        return new TargetPredicate.GraveyardCards(predicate, scope);
    }

    /** A card in exile matching {@code predicate}. */
    public static TargetPredicate exiledCards(CardPredicate predicate) {
        return new TargetPredicate.ExiledCards(predicate);
    }

    /** A spell or ability on the stack matching {@code predicate}. */
    public static TargetPredicate spells(StackEntryPredicate predicate) {
        return new TargetPredicate.Spells(predicate);
    }

    /**
     * Disjunction across kinds. A single option collapses to that option, so the result is always
     * in the canonical form {@link TargetPredicate.AnyOf} requires (at least two leaves, one per
     * kind).
     */
    public static TargetPredicate anyOf(TargetPredicate... options) {
        if (options.length == 1) {
            return options[0];
        }
        return new TargetPredicate.AnyOf(options);
    }

    /** {@link TargetCategory#PLAYER}. */
    public static TargetPredicate player() {
        return PLAYER;
    }

    /** {@link TargetCategory#PERMANENT}. */
    public static TargetPredicate permanent() {
        return PERMANENT;
    }

    /** {@link TargetCategory#CREATURE} — layer-aware, so an animated land qualifies (CR 613.1d). */
    public static TargetPredicate creature() {
        return CREATURE;
    }

    /** {@link TargetCategory#LAND}. */
    public static TargetPredicate land() {
        return LAND;
    }

    /** {@link TargetCategory#CREATURE_OR_PLANESWALKER}. */
    public static TargetPredicate creatureOrPlaneswalker() {
        return CREATURE_OR_PLANESWALKER;
    }

    /** {@link TargetCategory#PLAYER_OR_PERMANENT} — a player or <em>any</em> permanent. */
    public static TargetPredicate playerOrPermanent() {
        return PLAYER_OR_PERMANENT;
    }

    /** {@link TargetCategory#PLAYER_OR_PLANESWALKER}. */
    public static TargetPredicate playerOrPlaneswalker() {
        return PLAYER_OR_PLANESWALKER;
    }

    /** {@link TargetCategory#ANY_TARGET} — CR 115.4's creature / planeswalker / player. */
    public static TargetPredicate anyTarget() {
        return ANY_TARGET;
    }

    /** {@link TargetCategory#SPELL_ON_STACK}. */
    public static TargetPredicate spellOnStack() {
        return SPELL_ON_STACK;
    }

    /** {@link TargetCategory#GRAVEYARD_CARD} — the opponent-scoped default. */
    public static TargetPredicate graveyardCard() {
        return GRAVEYARD_CARD;
    }

    /** {@link TargetCategory#ANY_GRAVEYARD_CARD}. */
    public static TargetPredicate anyGraveyardCard() {
        return ANY_GRAVEYARD_CARD;
    }

    /** {@link TargetCategory#CONTROLLERS_GRAVEYARD_CARD}. */
    public static TargetPredicate controllersGraveyardCard() {
        return CONTROLLERS_GRAVEYARD_CARD;
    }

    /** {@link TargetCategory#EXILE_CARD}. */
    public static TargetPredicate exileCard() {
        return EXILE_CARD;
    }

    /**
     * The predicate equivalent to {@code category}, or {@code null} for
     * {@link TargetCategory#NONE} (which targets nothing and therefore restricts nothing).
     */
    public static TargetPredicate forCategory(TargetCategory category) {
        return switch (category) {
            case NONE -> null;
            case PLAYER -> player();
            case PERMANENT -> permanent();
            case CREATURE -> creature();
            case LAND -> land();
            case CREATURE_OR_PLANESWALKER -> creatureOrPlaneswalker();
            case PLAYER_OR_PERMANENT -> playerOrPermanent();
            case PLAYER_OR_PLANESWALKER -> playerOrPlaneswalker();
            case ANY_TARGET -> anyTarget();
            case SPELL_ON_STACK -> spellOnStack();
            case GRAVEYARD_CARD -> graveyardCard();
            case ANY_GRAVEYARD_CARD -> anyGraveyardCard();
            case CONTROLLERS_GRAVEYARD_CARD -> controllersGraveyardCard();
            case EXILE_CARD -> exileCard();
        };
    }

    /**
     * The {@link TargetCategory} whose {@link #forCategory(TargetCategory)} value is
     * {@code declaredTarget}, i.e. the inverse of that mapping.
     *
     * <p>Transitional bridge behind {@code TargetSpec.category()}, deleted together with the enum.
     * It is a lookup over the fourteen canonical factory values rather than a structural match, so
     * a predicate no category can express fails loudly instead of being silently rounded to the
     * nearest enum constant. Narrow with {@code TargetSpec.predicate()} (or a card-level
     * {@code TargetFilter}); do not hand-build a declared target outside these factories while the
     * enum still exists — see {@code agent-docs/TARGET_PREDICATE_PLAN.md}.</p>
     */
    public static TargetCategory categoryOf(TargetPredicate declaredTarget) {
        if (declaredTarget == null) {
            return TargetCategory.NONE;
        }
        TargetCategory category = CATEGORY_BY_TARGET.get(declaredTarget);
        if (category == null) {
            throw new IllegalArgumentException(
                    "No TargetCategory expresses " + declaredTarget + ". While TargetCategory still "
                            + "exists a declared target must come from a TargetPredicates factory; "
                            + "put any further restriction on TargetSpec.predicate() instead.");
        }
        return category;
    }

    private static Map<TargetPredicate, TargetCategory> buildCategoryIndex() {
        Map<TargetPredicate, TargetCategory> index = new HashMap<>();
        for (TargetCategory category : TargetCategory.values()) {
            TargetPredicate target = forCategory(category);
            if (target != null) {
                index.put(target, category);
            }
        }
        return Map.copyOf(index);
    }

    /**
     * {@code base} with its permanent leaf additionally restricted by {@code narrowing} — the
     * predicate-narrowing axis {@code TargetSpec.predicate()} carries. A base that admits no
     * permanent is returned unchanged: the narrowing is over a permanent target, and such a base
     * never makes a permanent legal in the first place.
     */
    public static TargetPredicate narrowPermanents(TargetPredicate base, PermanentPredicate narrowing) {
        if (base == null || narrowing == null) {
            return base;
        }
        TargetPredicate.Permanents leaf = (TargetPredicate.Permanents)
                base.leaf(TargetPredicate.Kind.PERMANENT).orElse(null);
        if (leaf == null) {
            return base;
        }
        PermanentPredicate combined = ANY_PERMANENT.equals(leaf.inner())
                ? narrowing
                : new PermanentAllOfPredicate(List.of(leaf.inner(), narrowing));
        return replacePermanents(base, permanents(combined));
    }

    private static TargetPredicate replacePermanents(TargetPredicate base, TargetPredicate replacement) {
        if (base instanceof TargetPredicate.AnyOf anyOf) {
            return new TargetPredicate.AnyOf(anyOf.options().stream()
                    .map(option -> option instanceof TargetPredicate.Permanents ? replacement : option)
                    .toList());
        }
        return replacement;
    }
}
