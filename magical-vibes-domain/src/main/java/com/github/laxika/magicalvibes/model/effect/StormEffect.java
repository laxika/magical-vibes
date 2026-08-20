package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger descriptor for {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_SELF_CAST}
 * implementing the Storm keyword (CR 702.40): "When you cast this spell, copy it for each spell cast
 * before it this turn. You may choose new targets for the copies."
 *
 * @param tokenCopy whether the copies enter the battlefield as tokens when they resolve
 *
 * <p>At cast time {@code TriggerCollectionService.checkSpellCastTriggers} snapshots the just-cast
 * spell, counts the spells cast before it this turn (all players), and queues a triggered ability
 * wrapping a {@link StormCopyEffect} that creates that many copies at resolution.
 */
public record StormEffect(boolean tokenCopy) implements CardEffect {

    public StormEffect() {
        this(false);
    }
}
