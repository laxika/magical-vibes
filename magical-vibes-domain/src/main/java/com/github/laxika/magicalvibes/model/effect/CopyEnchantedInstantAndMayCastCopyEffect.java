package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * "Copy the enchanted instant card. You may cast the copy without paying its mana cost. If you do, exile the
 * enchanted card and attach this Aura to another instant card in a graveyard" (Spellweaver Volute). The card is the
 * one the source Aura enchants as the ability resolves, or enchanted as it last existed on the battlefield (CR 608.2h).
 *
 * @param enchantedCardId {@code null} on the card; the copy's "you may cast" choice, which the resolving ability
 *                        hands to the may ability it queues, names the enchanted card it copied
 */
public record CopyEnchantedInstantAndMayCastCopyEffect(UUID enchantedCardId) implements CardEffect {

    public CopyEnchantedInstantAndMayCastCopyEffect() {
        this(null);
    }
}
