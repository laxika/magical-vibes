package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
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

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factories for {@link TargetPredicate}: the five kind leaves, the cross-kind disjunction, and one
 * named factory per declared target a card can name. <strong>Always build a declared target from
 * one of these</strong> — the named values are interned, so specs compare and hash cheaply, and the
 * set of things an effect may declare stays enumerable.
 *
 * <p>{@link #playerOrPermanent()} is "a player or <em>any</em> permanent" and {@link #anyTarget()}
 * is CR 115.4's "any target" — a creature, planeswalker, battle or player. They are structurally distinct
 * values; a reader that wants one specifically must compare against it
 * ({@code TargetSpec.declares}) rather than asking whether both players and permanents are
 * admitted, which cannot tell them apart.</p>
 *
 * <p>There is a single {@link #graveyardCard(GraveyardSearchScope)} rather than one factory per
 * zone state: which graveyards are searched is a {@link GraveyardSearchScope} component on the
 * leaf, read back through {@code TargetSpec.graveyardScope()}.</p>
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
    private static final PermanentPredicate IS_BATTLE = new PermanentIsBattlePredicate();
    private static final PermanentPredicate IS_CREATURE_OR_PLANESWALKER =
            new PermanentAnyOfPredicate(List.of(IS_CREATURE, IS_PLANESWALKER));

    /**
     * The permanent half of CR 115.4's "any target". Deliberately <em>not</em>
     * {@link #IS_CREATURE_OR_PLANESWALKER}: "target creature or planeswalker" is a narrower
     * restriction that must stay battle-free, so the two cannot share a leaf.
     */
    private static final PermanentPredicate IS_ANY_TARGET_PERMANENT =
            new PermanentAnyOfPredicate(List.of(IS_CREATURE, IS_PLANESWALKER, IS_BATTLE));

    /**
     * The canonical declared targets, interned. {@code TargetSpec} values are rebuilt on every
     * {@code CardEffect.targetSpec()} call and target enumeration calls that in a loop, so the
     * named factories hand back a shared immutable value rather than allocating one per call.
     */
    private static final TargetPredicate PLAYER = players(ANY_PLAYER);
    private static final TargetPredicate PERMANENT = permanents(ANY_PERMANENT);
    private static final TargetPredicate CREATURE = permanents(IS_CREATURE);
    private static final TargetPredicate LAND = permanents(IS_LAND);
    private static final TargetPredicate CREATURE_OR_PLANESWALKER = permanents(IS_CREATURE_OR_PLANESWALKER);
    private static final TargetPredicate PLAYER_OR_PERMANENT = anyOf(PLAYER, PERMANENT);
    private static final TargetPredicate PLAYER_OR_PLANESWALKER = anyOf(PLAYER, permanents(IS_PLANESWALKER));
    private static final TargetPredicate ANY_TARGET = anyOf(PLAYER, permanents(IS_ANY_TARGET_PERMANENT));
    private static final TargetPredicate SPELL_ON_STACK = spells(ANY_SPELL);
    private static final Map<GraveyardSearchScope, TargetPredicate> GRAVEYARD_CARD_BY_SCOPE =
            buildGraveyardCardIndex();
    private static final TargetPredicate EXILE_CARD = exiledCards(ANY_CARD);

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

    /** A player. */
    public static TargetPredicate player() {
        return PLAYER;
    }

    /** Any permanent on the battlefield. */
    public static TargetPredicate permanent() {
        return PERMANENT;
    }

    /** A creature — layer-aware, so an animated land qualifies (CR 613.1d). */
    public static TargetPredicate creature() {
        return CREATURE;
    }

    /** A land, layer-aware. */
    public static TargetPredicate land() {
        return LAND;
    }

    /** A creature or a planeswalker, layer-aware. */
    public static TargetPredicate creatureOrPlaneswalker() {
        return CREATURE_OR_PLANESWALKER;
    }

    /** A player or <em>any</em> permanent — declares no restriction on the permanent half. */
    public static TargetPredicate playerOrPermanent() {
        return PLAYER_OR_PERMANENT;
    }

    /** A player or a planeswalker, layer-aware. */
    public static TargetPredicate playerOrPlaneswalker() {
        return PLAYER_OR_PLANESWALKER;
    }

    /** CR 115.4's "any target" — a creature, planeswalker, battle or player, layer-aware. */
    public static TargetPredicate anyTarget() {
        return ANY_TARGET;
    }

    /** A spell on the stack (validated on the stack path, not by the spec interpreter). */
    public static TargetPredicate spellOnStack() {
        return SPELL_ON_STACK;
    }

    /**
     * A card in a graveyard within {@code scope}, with no card restriction.
     *
     * <p>Declare the scope the card's oracle text actually names; every reader takes its search
     * scope from here via {@code TargetSpec.graveyardScope()}, so a wrong scope widens or narrows
     * the target list directly.</p>
     */
    public static TargetPredicate graveyardCard(GraveyardSearchScope scope) {
        return GRAVEYARD_CARD_BY_SCOPE.get(scope);
    }

    /** A card in exile. */
    public static TargetPredicate exileCard() {
        return EXILE_CARD;
    }

    private static Map<GraveyardSearchScope, TargetPredicate> buildGraveyardCardIndex() {
        Map<GraveyardSearchScope, TargetPredicate> index = new EnumMap<>(GraveyardSearchScope.class);
        for (GraveyardSearchScope scope : GraveyardSearchScope.values()) {
            index.put(scope, graveyardCards(ANY_CARD, scope));
        }
        return index;
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
