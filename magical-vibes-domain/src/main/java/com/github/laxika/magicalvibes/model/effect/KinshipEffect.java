package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Kinship (Morningtide): "At the beginning of your upkeep, you may look at the top card of your
 * library. If it shares a creature type with this creature, you may reveal it. If you do,
 * [revealEffects]."
 *
 * <p>Resolves at upkeep: the controller looks at the top card of their library. If that card shares
 * a creature type with the source creature (Changeling counts as every creature type), the
 * controller is offered a "you may reveal" choice; on reveal, {@code revealEffects} resolve against
 * the source permanent. The card stays on top of the library either way. Placed in the
 * {@code UPKEEP_TRIGGERED} slot.</p>
 */
public record KinshipEffect(List<CardEffect> revealEffects) implements CardEffect {
}
