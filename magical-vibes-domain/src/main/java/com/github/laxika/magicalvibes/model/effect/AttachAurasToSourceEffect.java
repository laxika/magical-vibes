package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may attach to this creature any number of Auras on the battlefield and you may put onto the
 * battlefield attached to it any number of Aura cards that could enchant it from your graveyard
 * and/or hand" (Bruna, Light of Alabaster).
 *
 * <p>Both halves are one selection: the controller picks any number of objects out of a single
 * pool holding every Aura permanent on the battlefield plus every Aura card in their own graveyard
 * and hand. Only Auras that could legally enchant the source are offered (CR 701.3a — an Aura
 * can't be attached to something it couldn't enchant), so nothing chosen can fail to move.
 */
public record AttachAurasToSourceEffect() implements CardEffect {
}
