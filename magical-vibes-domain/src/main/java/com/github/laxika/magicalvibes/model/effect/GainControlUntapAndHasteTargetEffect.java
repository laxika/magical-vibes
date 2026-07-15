package com.github.laxika.magicalvibes.model.effect;

/**
 * The "Threaten" bundle collapsed into a single targeted effect: gain control of the target
 * permanent until end of turn, untap it, and give it haste until end of turn.
 *
 * <p>Threaten-style cards compose this from three separate {@link GainControlOfTargetEffect} +
 * {@link UntapPermanentsEffect} + {@link GrantKeywordEffect} effects, but a single bundled effect
 * is required when the whole rider must be gated behind one {@link MayEffect} choice (a
 * {@code MayEffect} wraps exactly one effect). Dominus of Fealty's "you may gain control of target
 * permanent until end of turn. If you do, untap it and it gains haste until end of turn" is
 * expressed as {@code MayEffect(new GainControlUntapAndHasteTargetEffect())}.
 */
public record GainControlUntapAndHasteTargetEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
