package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * Static effect that grants lifelink to instant or sorcery spells of the specified color
 * controlled by this permanent's controller. Checked in DamageResolutionService when
 * spell damage is dealt. Used by Firesong and Sunspeaker.
 */
public record GrantLifelinkToControllerSpellsByColorEffect(CardColor color) implements CardEffect {
}
