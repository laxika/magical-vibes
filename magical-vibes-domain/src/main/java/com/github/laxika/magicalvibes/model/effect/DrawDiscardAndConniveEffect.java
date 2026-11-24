package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.Objects;

/**
 * "[Permanent] connives [N]" (CR 701.50a): the conniving permanent's controller draws and then discards that many cards,
 * and the permanent gets a +1/+1 counter for each nonland card discarded this way. Any permanent can connive; the card's
 * own target filter narrows which permanents are legal ("target creature connives", "target Villain you control
 * connives"). A permanent that left the battlefield still connives, by its last known information (CR 701.50b), and
 * it connives even if some or all of the actions were impossible (CR 701.50f).
 *
 * <p>When the subject names several permanents ("each of X target creatures you control connive", "each creature that
 * convoked this spell connives"), they connive one at a time (CR 701.50c): the first player in APNAP order who controls
 * (or last controlled) one of them chooses which connives next, and so on for the rest.
 *
 * @param amount  how many cards are drawn and discarded
 * @param subject who connives: {@code source()} ("this creature connives"), {@code target()} ("target creature
 *                connives", every target of a bound multi-target group), {@code triggering()} ("it connives"),
 *                {@code convokedThisSpell()} (Lethal Scheme) or one named permanent
 */
public record DrawDiscardAndConniveEffect(DynamicAmount amount, PermanentSubject subject)
        implements CardDrawingEffect {

    public DrawDiscardAndConniveEffect {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(subject, "subject");
    }

    /** "This creature connives N." */
    public DrawDiscardAndConniveEffect(DynamicAmount amount) {
        this(amount, Subjects.source());
    }

    /** "Target creature connives" when {@code targetPermanent}, else "this creature connives". */
    public DrawDiscardAndConniveEffect(boolean targetPermanent) {
        this(new Fixed(1), targetPermanent);
    }

    /** "This creature connives." */
    public DrawDiscardAndConniveEffect() {
        this(new Fixed(1), Subjects.source());
    }

    /** "Target creature connives N" when {@code targetPermanent}, else "this creature connives N". */
    public DrawDiscardAndConniveEffect(DynamicAmount amount, boolean targetPermanent) {
        this(amount, targetPermanent ? Subjects.target() : Subjects.source());
    }

    /** "It connives": the stack entry's triggering permanent connives. */
    public static DrawDiscardAndConniveEffect ofTriggeringPermanent() {
        return ofTriggeringPermanent(new Fixed(1));
    }

    /** "It connives X" (Mask of the Schemer): the stack entry's triggering permanent connives {@code amount}. */
    public static DrawDiscardAndConniveEffect ofTriggeringPermanent(DynamicAmount amount) {
        return new DrawDiscardAndConniveEffect(amount, Subjects.triggering());
    }

    /** "[Permanents] connive": each permanent {@code subject} names connives once, one at a time. */
    public static DrawDiscardAndConniveEffect of(PermanentSubject subject) {
        return new DrawDiscardAndConniveEffect(new Fixed(1), subject);
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return amount;
    }

    @Override
    public TargetSpec targetSpec() {
        if (subject.declaresTarget()) {
            return TargetSpec.benign(TargetPredicates.permanent());
        }
        return subject instanceof PermanentSubject.Source || subject instanceof PermanentSubject.Triggering
                ? new TargetSpec(null, false, null, true, 1)
                : TargetSpec.NONE;
    }
}
