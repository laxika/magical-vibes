package com.github.laxika.magicalvibes.model.effect;

/**
 * Offers a permanent spell from the controller's hand with mana value at most the triggering
 * spell's mana value. If every such spell is declined, offers a land card from that hand to be
 * put onto the battlefield.
 */
public record MayCastPermanentSpellFromHandOrPutLandEffect()
        implements TriggeringSpellManaValueEffect {
}
