package com.github.laxika.magicalvibes.model.replacement;

import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A token creation event whose token count replacement effects (Xorn, Doubling Season, Halving
 * Season) are being ordered. The player creating the tokens orders the effects (CR 616.1); once all
 * of them have applied, {@link #amount()} is the number of tokens to create from {@link #token()}.
 *
 * @param controllerId      the player creating the tokens, who orders the effects
 * @param amount            the number of tokens as the event currently stands
 * @param token             the blueprint of the tokens being created
 * @param power             the evaluated power of the tokens
 * @param toughness         the evaluated toughness of the tokens
 * @param sourceSetCode     the set code of the source creating the tokens
 * @param additionalEffects extra effects the created tokens gain, never {@code null}
 * @param creatureToken     whether the tokens are creatures, which decides whether creature-only effects apply
 */
public record TokenCreationEvent(UUID controllerId, int amount, Set<String> appliedKeys, CreateTokenEffect token,
                                 int power, int toughness, String sourceSetCode,
                                 Map<EffectSlot, List<EffectRegistration>> additionalEffects,
                                 boolean creatureToken) implements ReplacementEvent {

    public TokenCreationEvent {
        appliedKeys = Set.copyOf(appliedKeys);
        additionalEffects = additionalEffects == null ? Map.of() : additionalEffects;
    }

    @Override
    public UUID chooserId() {
        return controllerId;
    }

    public TokenCreationEvent withAmount(int newAmount) {
        return new TokenCreationEvent(controllerId, newAmount, appliedKeys, token, power, toughness,
                sourceSetCode, additionalEffects, creatureToken);
    }

    @Override
    public TokenCreationEvent withApplied(String key) {
        Set<String> keys = new HashSet<>(appliedKeys);
        keys.add(key);
        return new TokenCreationEvent(controllerId, amount, keys, token, power, toughness,
                sourceSetCode, additionalEffects, creatureToken);
    }

    @Override
    public TokenCreationEvent withoutAppliedKeys() {
        return new TokenCreationEvent(controllerId, amount, Set.of(), token, power, toughness,
                sourceSetCode, additionalEffects, creatureToken);
    }
}
