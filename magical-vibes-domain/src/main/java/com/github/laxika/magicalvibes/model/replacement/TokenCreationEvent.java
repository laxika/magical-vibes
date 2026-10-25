package com.github.laxika.magicalvibes.model.replacement;

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

/**
 * A token creation event whose replacement effects are being ordered: count replacements (Xorn,
 * Doubling Season, Parallel Lives, Halving Season) and "those tokens plus ..." additions (Queen
 * Allenal of Ruadach, Quina Qu Gourmet, Chatterfang). The player creating the tokens orders the
 * effects (CR 616.1); once all of them have applied, {@link #tokenCount()} tokens are created from
 * {@link #token()} together with every group of {@link #addedTokens()}.
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
 */
public record TokenCreationEvent(UUID controllerId, int tokenCount, Set<String> appliedKeys, CreateTokenEffect token,
                                 int power, int toughness, String sourceSetCode,
                                 Map<EffectSlot, List<EffectRegistration>> additionalEffects,
                                 boolean creatureToken, List<TokenGroup> addedTokens) implements ReplacementEvent {

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
                creatureToken, List.of());
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

    public TokenCreationEvent withTokenCount(int newTokenCount) {
        return new TokenCreationEvent(controllerId, newTokenCount, appliedKeys, token, power, toughness,
                sourceSetCode, additionalEffects, creatureToken, addedTokens);
    }

    public TokenCreationEvent withAddedTokens(List<TokenGroup> newAddedTokens) {
        return new TokenCreationEvent(controllerId, tokenCount, appliedKeys, token, power, toughness,
                sourceSetCode, additionalEffects, creatureToken, newAddedTokens);
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
                sourceSetCode, additionalEffects, creatureToken, addedTokens);
    }

    @Override
    public TokenCreationEvent withoutAppliedKeys() {
        return new TokenCreationEvent(controllerId, tokenCount, Set.of(), token, power, toughness,
                sourceSetCode, additionalEffects, creatureToken, addedTokens);
    }
}
