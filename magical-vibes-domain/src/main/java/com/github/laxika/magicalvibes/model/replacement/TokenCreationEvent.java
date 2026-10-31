package com.github.laxika.magicalvibes.model.replacement;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * A token creation event whose replacement effects are being ordered: count replacements (Xorn,
 * Doubling Season, Parallel Lives, Halving Season), "those tokens plus ..." additions (Queen
 * Allenal of Ruadach, Quina Qu Gourmet, Chatterfang, Worldwalker Helm, Stridehangar Automaton, Donatello,
 * Tippy-Toe, a solved Case of the Pilfered Proof, Bilbo), Academy Manufactor's "one of each" and the replacements
 * that change what kind of token is created (Divine Visitation, Fisher's Talent). The
 * player creating the tokens orders the effects (CR 616.1); once all of them have applied, {@link #tokenCount()} tokens are created from
 * {@link #token()} together with every group of {@link #addedTokens()}.
 *
 * <p>An event in which a token-copy effect (populate, CR 701.36a, included) creates copies, described by
 * {@link #copyBatch()}, has no blueprint of its own: {@link #copyOf()}
 * is the token being copied, {@link #tokenCount()} counts the copies and {@link #token()} only describes their kind
 * (types, subtypes, name and colour) for the effects that look at it. A token-copy effect copying several
 * different cards in one event puts the copies of each other card in a copy group of {@link #addedTokens()}
 * ({@link TokenGroup#copyOf()}), so a count replacement rewrites each card's copies.
 *
 * @param controllerId      the player creating the tokens, who orders the effects
 * @param tokenCount        the number of tokens from {@link #token()} as the event currently stands
 * @param token             the blueprint of the tokens being created
 * @param power             the evaluated power of the tokens
 * @param toughness         the evaluated toughness of the tokens
 * @param sourceSetCode     the set code of the source creating the tokens
 * @param additionalAbilities extra abilities the created tokens of {@link #token()} gain, never {@code null}
 * @param creatureToken     whether the tokens of {@link #token()} are creatures, which decides whether creature-only effects apply
 * @param addedTokens       the other tokens created by the same event: the blueprint's simultaneous tokens and the
 *                          tokens replacement effects added, one group per distinct blueprint in a canonical order
 * @param copyOf            the token whose copies {@link #tokenCount()} counts, or {@code null} when they are created
 *                          from {@link #token()}
 * @param copyBatch         how a token-copy effect builds and enters its copies of {@link #copyOf()}, or {@code null}
 *                          for blueprint events
 */
public record TokenCreationEvent(UUID controllerId, int tokenCount, Set<String> appliedKeys, CreateTokenEffect token,
                                 int power, int toughness, String sourceSetCode,
                                 List<Ability> additionalAbilities,
                                 boolean creatureToken, List<TokenGroup> addedTokens, Card copyOf,
                                 TokenCopyBatch copyBatch) implements ReplacementEvent {

    private static final Comparator<TokenGroup> GROUP_ORDER = Comparator
            .comparing((TokenGroup group) -> group.blueprint().tokenName())
            .thenComparing(group -> group.blueprint().toString());

    /**
     * {@code count} tokens of one blueprint that the event creates besides those of {@link #token()}.
     *
     * <p>A token-copy event that copies several different cards ({@link #copyBatch()}) holds the copies of every
     * card but {@link #copyOf()} as copy groups: {@code copyOf} is the card those tokens copy, {@code blueprint}
     * only describes their kind and {@code copyCreature} says whether they enter as creatures.
     *
     * <p>When Jinnie Fay, Divine Visitation or Fisher's Talent replaces tokens of a token-copy event, the replacing
     * tokens are substitute groups ({@code substitute}): tokens created from {@code blueprint} in place of tokens the creating effect makes, which
     * keep its instructions, and {@code copyOf} is then the card the tokens they replace copied, or {@code null} when
     * they replace tokens a replacement added.
     *
     * @param blueprint    the blueprint of the tokens, or the kind of the copies of a copy group
     * @param count        the number of tokens as the event currently stands
     * @param copyOf       the card a copy group's tokens copy, the card the replaced copies of a substitute group
     *                     copied, or {@code null} for tokens created from {@code blueprint}
     * @param copyCreature whether a copy group's tokens are creatures; ignored for blueprint and substitute groups
     * @param substitute   whether the tokens are created from {@code blueprint} in place of tokens of the creating
     *                     effect (Jinnie Fay, Divine Visitation, Fisher's Talent in a token-copy event)
     */
    public record TokenGroup(CreateTokenEffect blueprint, int count, Card copyOf, boolean copyCreature,
                             boolean substitute) {

        public TokenGroup(CreateTokenEffect blueprint, int count) {
            this(blueprint, count, null, false, false);
        }

        /** {@code count} copies of {@code copyOf}, whose kind {@code kind} describes. */
        public static TokenGroup copies(CreateTokenEffect kind, int count, Card copyOf, boolean creature) {
            return new TokenGroup(kind, count, copyOf, creature, false);
        }

        /**
         * {@code count} tokens of {@code blueprint} created in place of tokens of the creating effect, which were
         * copies of {@code replacedCopyOf}, or tokens a replacement added when it is {@code null}.
         */
        public static TokenGroup substitutes(CreateTokenEffect blueprint, int count, Card replacedCopyOf) {
            return new TokenGroup(blueprint, count, replacedCopyOf, false, true);
        }

        /** Whether the group's tokens are copies of {@link #copyOf()} rather than created from {@link #blueprint()}. */
        public boolean copies() {
            return copyOf != null && !substitute;
        }

        public boolean creature() {
            return copies() ? copyCreature : blueprint.primaryType() == CardType.CREATURE;
        }

        /** This group with {@code newCount} tokens. */
        public TokenGroup withCount(int newCount) {
            return new TokenGroup(blueprint, newCount, copyOf, copyCreature, substitute);
        }

        private boolean sameTokens(TokenGroup other) {
            return copyOf == other.copyOf && substitute == other.substitute && blueprint.equals(other.blueprint);
        }
    }

    public TokenCreationEvent {
        appliedKeys = Set.copyOf(appliedKeys);
        additionalAbilities = additionalAbilities == null ? List.of() : List.copyOf(additionalAbilities);
        addedTokens = canonical(addedTokens == null ? List.of() : addedTokens);
    }

    public TokenCreationEvent(UUID controllerId, int tokenCount, Set<String> appliedKeys, CreateTokenEffect token,
                              int power, int toughness, String sourceSetCode,
                              List<Ability> additionalAbilities, boolean creatureToken) {
        this(controllerId, tokenCount, appliedKeys, token, power, toughness, sourceSetCode, additionalAbilities,
                creatureToken, List.of(), null, null);
    }

    public TokenCreationEvent(UUID controllerId, int tokenCount, Set<String> appliedKeys, CreateTokenEffect token,
                              int power, int toughness, String sourceSetCode,
                              List<Ability> additionalAbilities, boolean creatureToken,
                              List<TokenGroup> addedTokens) {
        this(controllerId, tokenCount, appliedKeys, token, power, toughness, sourceSetCode, additionalAbilities,
                creatureToken, addedTokens, null, null);
    }

    /**
     * An event in which a token-copy effect creates {@code count} copies of {@code copyOf}, built and entered as
     * {@code batch} describes once the event's replacements have applied.
     */
    public static TokenCreationEvent copies(UUID controllerId, int count, Card copyOf, CreateTokenEffect kind,
                                            boolean creatureToken, String sourceSetCode, TokenCopyBatch batch) {
        return new TokenCreationEvent(controllerId, count, Set.of(), kind, 0, 0, sourceSetCode, List.of(),
                creatureToken, List.of(), copyOf, batch);
    }

    /** Merges groups of equal blueprints copying the same card, if any, and sorts them, so the order effects added them in doesn't matter. */
    private static List<TokenGroup> canonical(List<TokenGroup> groups) {
        List<TokenGroup> merged = new ArrayList<>();
        for (TokenGroup group : groups) {
            int index = -1;
            for (int i = 0; i < merged.size(); i++) {
                if (merged.get(i).sameTokens(group)) {
                    index = i;
                    break;
                }
            }
            if (index < 0) {
                merged.add(group);
            } else {
                merged.set(index, group.withCount(merged.get(index).count() + group.count()));
            }
        }
        merged.removeIf(group -> group.count() <= 0);
        merged.sort(GROUP_ORDER);
        return List.copyOf(merged);
    }

    @Override
    public UUID chooserId() {
        return controllerId;
    }

    /** The total number of tokens the event creates as it currently stands. */
    @Override
    public int amount() {
        return tokenCount + addedTokens.stream().mapToInt(TokenGroup::count).sum();
    }

    /** Whether the event creates at least one creature token as it currently stands. */
    public boolean createsCreatureToken() {
        return creatureToken && tokenCount > 0 || addedTokens.stream().anyMatch(TokenGroup::creature);
    }

    /** Whether the event creates at least one token matching {@code kind} as it currently stands. */
    public boolean createsToken(Predicate<CreateTokenEffect> kind) {
        return countTokens(kind) > 0;
    }

    /** How many of the tokens the event creates as it currently stands match {@code kind}. */
    public int countTokens(Predicate<CreateTokenEffect> kind) {
        int count = tokenCount > 0 && kind.test(token) ? tokenCount : 0;
        for (TokenGroup group : addedTokens) {
            if (kind.test(group.blueprint())) {
                count += group.count();
            }
        }
        return count;
    }

    public TokenCreationEvent withTokenCount(int newTokenCount) {
        return new TokenCreationEvent(controllerId, newTokenCount, appliedKeys, token, power, toughness,
                sourceSetCode, additionalAbilities, creatureToken, addedTokens, copyOf, copyBatch);
    }

    public TokenCreationEvent withAddedTokens(List<TokenGroup> newAddedTokens) {
        return new TokenCreationEvent(controllerId, tokenCount, appliedKeys, token, power, toughness,
                sourceSetCode, additionalAbilities, creatureToken, newAddedTokens, copyOf, copyBatch);
    }

    /**
     * This event creating its {@link #tokenCount()} tokens from {@code newToken} instead, a replacement that changes
     * what is created (Divine Visitation, Fisher's Talent): the replaced tokens' abilities, including the
     * {@link #additionalAbilities()} they would have been created with, are gone.
     */
    public TokenCreationEvent withToken(CreateTokenEffect newToken, int newPower, int newToughness,
                                        boolean newCreatureToken) {
        return new TokenCreationEvent(controllerId, tokenCount, appliedKeys, newToken, newPower, newToughness,
                sourceSetCode, List.of(), newCreatureToken, addedTokens, copyOf, copyBatch);
    }

    /** This event with {@code count} more tokens of {@code blueprint}. */
    public TokenCreationEvent plus(CreateTokenEffect blueprint, int count) {
        List<TokenGroup> groups = new ArrayList<>(addedTokens);
        groups.add(new TokenGroup(blueprint, count));
        return withAddedTokens(groups);
    }

    @Override
    public TokenCreationEvent withApplied(String key) {
        Set<String> keys = new HashSet<>(appliedKeys);
        keys.add(key);
        return new TokenCreationEvent(controllerId, tokenCount, keys, token, power, toughness,
                sourceSetCode, additionalAbilities, creatureToken, addedTokens, copyOf, copyBatch);
    }

    @Override
    public TokenCreationEvent withoutAppliedKeys() {
        return new TokenCreationEvent(controllerId, tokenCount, Set.of(), token, power, toughness,
                sourceSetCode, additionalAbilities, creatureToken, addedTokens, copyOf, copyBatch);
    }
}
