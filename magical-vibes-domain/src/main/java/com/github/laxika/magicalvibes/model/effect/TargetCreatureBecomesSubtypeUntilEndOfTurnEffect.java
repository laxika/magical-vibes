package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * One-shot effect: the targeted creature <em>becomes</em> the given creature type until end of turn,
 * replacing all of its other creature types (e.g. Boldwyr Intimidator: "{R}: Target creature becomes
 * a Coward until end of turn."). Sets {@code Permanent.transientCreatureTypeOverride}, which the
 * layered pass reads to strip every creature subtype and add this one. Cleared at end of turn by
 * {@code resetModifiers()}. Contrast {@link GrantSubtypeToTargetCreatureEffect}, which is permanent
 * and additive ("in addition to its other types").
 *
 * @param subtype the creature type the target becomes
 */
public record TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype subtype) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }
}
