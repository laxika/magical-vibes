package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement marker for effects that create additional copies of a spell.
 *
 * <p>Each active instance adds one copy to a spell-copy event. The copy pipeline owns the
 * replacement because it must apply once to the whole event rather than recursively to each
 * copy it creates.</p>
 */
public record AdditionalSpellCopyEffect() implements CardEffect {
}
