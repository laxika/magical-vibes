package com.github.laxika.magicalvibes.model.effect;

/**
 * Marks an ordered effect whose target groups include both a graveyard card and a player.
 *
 * <p>The ETB targeting pipeline must keep this as one multi-target ability instead of routing
 * its graveyard target through the standalone graveyard-target path.</p>
 */
public interface TargetedGraveyardAndPlayerEffect extends CardEffect {
}
