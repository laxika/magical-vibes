package com.github.laxika.magicalvibes.model.effect;

/**
 * Counter the triggering spell if a card with the same name is in a graveyard, or a nontoken
 * permanent with the same name is on the battlefield. Bazaar of Wonders.
 *
 * <p>Used both as the {@code ON_ANY_PLAYER_CASTS_SPELL} trigger descriptor and as the resolved
 * effect: the collector stamps the cast spell onto the trigger's {@code targetId} (zone
 * {@code STACK}) and the condition is checked on resolution, not on collection — it is a
 * conditional part of the effect, not an intervening-if clause.
 */
public record CounterSpellIfNameFoundElsewhereEffect() implements CounterSpellingEffect {

    @Override public TargetSpec targetSpec() { return TargetSpec.benign(TargetCategory.SPELL_ON_STACK); }
}
