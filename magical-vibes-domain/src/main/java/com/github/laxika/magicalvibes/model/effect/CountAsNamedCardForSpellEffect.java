package com.github.laxika.magicalvibes.model.effect;

/**
 * While this card is in a graveyard, effects from spells named {@code spellName} count it
 * as a card named {@code cardName}. This does not change the card's actual name.
 * Consumed by the engine's graveyard amount evaluation, not battlefield bonus calculation.
 */
public record CountAsNamedCardForSpellEffect(String spellName, String cardName)
        implements GraveyardStaticEffect {
}
