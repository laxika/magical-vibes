package com.github.laxika.magicalvibes.model.replacement;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * A token creation event whose replacement effects are being ordered: count replacements (Xorn,
 * Doubling Season, Parallel Lives, Halving Season), "those tokens plus ..." additions (Queen
 * Allenal of Ruadach, Quina Qu Gourmet, Chatterfang, Worldwalker Helm, Stridehangar Automaton, Donatello,
 * Tippy-Toe, a solved Case of the Pilfered Proof, Bilbo) and Academy Manufactor's "one of each". The
 * player creating the tokens orders the effects (CR 616.1); once all of them have applied, {@link #tokenCount()} tokens are created from
 * {@link #token()} together with every group of {@link #addedTokens()}.
 *
 * <p>An event that creates copies of a token (populate, CR 701.36a, or a token-copy effect described by
 * {@link #copyBatch()}) has no blueprint of its own: {@link #copyOf()}
 * is the token being copied, {@link #tokenCount()} counts the copies and {@link #token()} only describes their kind
 * (types, subtypes, name and colour) for the effects that look at it.
 *
 * @param controllerId      the player creating the tokens, who orders the effects
 * @param tokenCount        the number of tokens from {@link #token()} as the event currently stands
 * @param token             the blueprint of the tokens being created
 * @param power             the evaluated power of the tokens
 * @param toughness         the evaluated toughness of the tokens
 * @param sourceSetCode     the set code of the source creating the tokens
 * @param additionalEffects extra effects the created tokens of {@link #token()} gain, never {@code null}
 * @param creatureToken     whether the tokens of {@link #token()} are creatures, which decides whether creature-only effects apply
 * @param addedTokens       the other tokens created by the same event: the blueprint's simultaneous tokens and the
 *                          tokens replacement effects added, one group per distinct blueprint in a canonical order
 * @param copyOf            the token whose copies {@link #tokenCount()} counts, or {@code null} when they are created
 *                          from {@link #token()}
 * @param copyBatch         how a token-copy effect builds and enters its copies of {@link #copyOf()}, or {@code null}
 *                          for populate and blueprint events
 */
public record TokenCreationEvent(UUID controllerId, int tokenCount, Set<String> appliedKeys, CreateTokenEffect token,
                                 int power, int toughness, String sourceSetCode,
                                 Map<EffectSlot, List<EffectRegistration>> additionalEffects,
                                 boolean creatureToken, List<TokenGroup> addedTokens, Card copyOf,
                                 TokenCopyBatch copyBatch) implements ReplacementEvent {

    private static final Comparator<TokenGroup> GROUP_ORDER = Comparator
            .comparing((TokenGroup group) -> group.blueprint().tokenName())
            .thenComparing(group -> group.blueprint().toString());

    /**
     * {@code count} tokens of one blueprint that the event creates besides those of {@link #token()}.
     */
    public record TokenGroup(CreateTokenEffect blueprint, int count) {

        public boolean creature() {
            return blueprint.primaryType() == CardType.CREATURE;
        }
    }

    public TokenCreationEvent {
        appliedKeys = Set.copyOf(appliedKeys);
        additionalEffects = additionalEffects == null ? Map.of() : additionalEffects;
        addedTokens = canonical(addedTokens == null ? List.of() : addedTokens);
    }

    public TokenCreationEvent(UUID controllerId, int tokenCount, Set<String> appliedKeys, CreateTokenEffect token,
                              int power, int toughness, String sourceSetCode,
                              Map<EffectSlot, List<EffectRegistration>> additionalEffects, boolean creatureToken) {
        this(controllerId, tokenCount, appliedKeys, token, power, toughness, sourceSetCode, additionalEffects,
                creatureToken, List.of(), null, null);
    }

    public TokenCreationEvent(UUID controllerId, int tokenCount, Set<String> appliedKeys, CreateTokenEffect token,
                              int power, int toughness, String sourceSetCode,
                              Map<EffectSlot, List<EffectRegistration>> additionalEffects, boolean creatureToken,
                              List<TokenGroup> addedTokens) {
        this(controllerId, tokenCount, appliedKeys, token, power, toughness, sourceSetCode, additionalEffects,
                creatureToken, addedTokens, null, null);
    }

    /**
     * An event creating {@code count} copies of {@code copyOf}; {@code kind} describes those copies for the
     * effects that look at the kind of token being created.
     */
    public static TokenCreationEvent copies(UUID controllerId, int count, Card copyOf, CreateTokenEffect kind,
                                            boolean creatureToken, String sourceSetCode) {
        return new TokenCreationEvent(controllerId, count, Set.of(), kind, 0, 0, sourceSetCode, Map.of(),
                creatureToken, List.of(), copyOf, null);
    }

    /**
     * An event in which a token-copy effect creates {@code count} copies of {@code copyOf}, built and entered as
     * {@code batch} describes once the event's replacements have applied.
     */
    public static TokenCreationEvent copies(UUID controllerId, int count, Card copyOf, CreateTokenEffect kind,
                                            boolean creatureToken, String sourceSetCode, TokenCopyBatch batch) {
        return new TokenCreationEvent(controllerId, count, Set.of(), kind, 0, 0, sourceSetCode, Map.of(),
                creatureToken, List.of(), copyOf, batch);
    }

    /** Merges groups of equal blueprints and sorts them, so the order effects added them in doesn't matter. */
    private static List<TokenGroup> canonical(List<TokenGroup> groups) {
        List<TokenGroup> merged = new ArrayList<>();
        for (TokenGroup group : groups) {
            int index = -1;
            for (int i = 0; i < merged.size(); i++) {
                if (merged.get(i).blueprint().equals(group.blueprint())) {
                    index = i;
                    break;
                }
            }
            if (index < 0) {
                merged.add(group);
            } else {
                merged.set(index, new TokenGroup(group.blueprint(), merged.get(index).count() + group.count()));
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
                sourceSetCode, additionalEffects, creatureToken, addedTokens, copyOf, copyBatch);
    }

    public TokenCreationEvent withAddedTokens(List<TokenGroup> newAddedTokens) {
        return new TokenCreationEvent(controllerId, tokenCount, appliedKeys, token, power, toughness,
                sourceSetCode, additionalEffects, creatureToken, newAddedTokens, copyOf, copyBatch);
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
                sourceSetCode, additionalEffects, creatureToken, addedTokens, copyOf, copyBatch);
    }

    @Override
    public TokenCreationEvent withoutAppliedKeys() {
        return new TokenCreationEvent(controllerId, tokenCount, Set.of(), token, power, toughness,
                sourceSetCode, additionalEffects, creatureToken, addedTokens, copyOf, copyBatch);
    }
}
