package com.github.laxika.magicalvibes.model.effect;

/**
 * "Whenever a [creature] you control enters, [you may] transform it."
 *
 * <p>Trigger-materialising marker for {@code ON_ALLY_CREATURE_ENTERS_BATTLEFIELD}. Unlike
 * {@link TransformSelfEffect} this transforms the entering creature ("it"), not the source. The
 * enter collector resolves the entering permanent and, when {@code optional}, queues a
 * {@code MayEffect(TransformTargetPermanentEffect)} with {@code targetId} set to that creature and
 * {@code sourcePermanentId} set to this permanent; when not {@code optional} it queues the
 * transform directly. Gate the subtype with {@link TriggeringCardConditionalEffect} (Vildin-Pack
 * Alpha: Werewolf).
 */
public record TransformEnteringCreatureEffect(boolean optional) implements CardEffect {

    /** "You may transform it" (optional). */
    public TransformEnteringCreatureEffect() {
        this(true);
    }
}
