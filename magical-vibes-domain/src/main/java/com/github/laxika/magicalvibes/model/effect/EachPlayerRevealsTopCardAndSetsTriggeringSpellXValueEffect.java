package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top card of each player's library and sets the triggering spell's X value to the
 * total mana value of the revealed cards. The revealed cards remain on top of their libraries.
 */
public record EachPlayerRevealsTopCardAndSetsTriggeringSpellXValueEffect()
        implements TriggeringSpellReferencingEffect {
}
