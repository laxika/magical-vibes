package com.github.laxika.magicalvibes.model.replacement;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

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
 * @param spellOnly   whether the mana can be spent only to cast spells (a land tapped through Piracy)
 * @param restriction the spending restriction the mana ability puts on its mana, or {@code null};
 *                    it stays on the mana whatever type and amount the replacements make it
 *                    (CR 106.6a)
 */
public record LandManaEvent(UUID playerId, UUID permanentId, Set<ManaColor> colors, int amount,
                            ChoiceContext.ManaColorChoice production, Set<String> appliedKeys, boolean spellOnly,
                            ManaRestriction restriction)
        implements ReplacementEvent {

    public LandManaEvent {
        colors = Set.copyOf(colors);
        appliedKeys = Set.copyOf(appliedKeys);
    }

    public LandManaEvent(UUID playerId, UUID permanentId, Set<ManaColor> colors, int amount,
                         ChoiceContext.ManaColorChoice production, Set<String> appliedKeys, boolean spellOnly) {
        this(playerId, permanentId, colors, amount, production, appliedKeys, spellOnly, null);
    }

    @Override
    public UUID chooserId() {
        return playerId;
    }

    public LandManaEvent withColors(Set<ManaColor> newColors) {
        return new LandManaEvent(playerId, permanentId, newColors, amount, production, appliedKeys, spellOnly, restriction);
    }

    public LandManaEvent withAmount(int newAmount) {
        return new LandManaEvent(playerId, permanentId, colors, newAmount, production, appliedKeys, spellOnly, restriction);
    }

    @Override
    public LandManaEvent withApplied(String key) {
        Set<String> keys = new HashSet<>(appliedKeys);
        keys.add(key);
        return new LandManaEvent(playerId, permanentId, colors, amount, production, keys, spellOnly, restriction);
    }

    @Override
    public LandManaEvent withoutAppliedKeys() {
        return new LandManaEvent(playerId, permanentId, colors, amount, production, Set.of(), spellOnly, restriction);
    }
}
