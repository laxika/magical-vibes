package com.github.laxika.magicalvibes.model.effect;

/**
 * Static self-replacement: "If this creature would be destroyed, regenerate it."
 * Unlike a one-shot regeneration shield ({@link RegenerateEffect}), this is an always-on
 * intrinsic ability — it regenerates the creature every time it would be destroyed, as many
 * times as needed, without consuming a shield. Placed in {@code EffectSlot.STATIC} and honored
 * by {@code GraveyardService.tryRegenerate}. Used by Mossbridge Troll.
 */
public record RegeneratesIfWouldBeDestroyedEffect() implements CardEffect {
}
