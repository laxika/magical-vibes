package com.github.laxika.magicalvibes.model.effect;

/**
 * Eye of the Storm's "exile it. Then that player copies each instant or sorcery card exiled with this enchantment. For
 * each copy, the player may cast the copy without paying its mana cost", on a filtered
 * {@code Triggers.playerCastsSpell()} ability (an instant or sorcery card: a cast copy doesn't trigger it).
 * <p>
 * It reads the trigger event from the stack entry as it resolves: "it" is the triggering spell (exiled only while it is
 * still on the stack), "that player" the caster (the entry's non-targeting {@code targetId}), and the cards exiled with
 * the enchantment are those of the entry's source permanent.
 */
public record EyeOfTheStormExileAndCopyEffect() implements CardEffect {
}
