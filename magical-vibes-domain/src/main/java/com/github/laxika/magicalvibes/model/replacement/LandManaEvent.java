package com.github.laxika.magicalvibes.model.replacement;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Mana a land is about to produce while replacement effects such as Deep Water, Infernal
 * Darkness or Mana Reflection modify it. {@code colors} holds the single type the land produces,
 * or every color the player may still pick from; {@code amount} is the total mana.
 *
 * @param playerId    the player tapping the land, who receives the mana and orders the effects
 * @param permanentId the tapped land
 * @param production  the plain mana-color choice describing the source's tags; its amount and
 *                    color options are ignored until the event is finished
 */
public record LandManaEvent(UUID playerId, UUID permanentId, Set<ManaColor> colors, int amount,
                            ChoiceContext.ManaColorChoice production, Set<String> appliedKeys)
        implements ReplacementEvent {

    public LandManaEvent {
        colors = Set.copyOf(colors);
        appliedKeys = Set.copyOf(appliedKeys);
    }

    @Override
    public UUID chooserId() {
        return playerId;
    }

    public LandManaEvent withColors(Set<ManaColor> newColors) {
        return new LandManaEvent(playerId, permanentId, newColors, amount, production, appliedKeys);
    }

    public LandManaEvent withAmount(int newAmount) {
        return new LandManaEvent(playerId, permanentId, colors, newAmount, production, appliedKeys);
    }

    @Override
    public LandManaEvent withApplied(String key) {
        Set<String> keys = new HashSet<>(appliedKeys);
        keys.add(key);
        return new LandManaEvent(playerId, permanentId, colors, amount, production, keys);
    }

    @Override
    public LandManaEvent withoutAppliedKeys() {
        return new LandManaEvent(playerId, permanentId, colors, amount, production, Set.of());
    }
}
