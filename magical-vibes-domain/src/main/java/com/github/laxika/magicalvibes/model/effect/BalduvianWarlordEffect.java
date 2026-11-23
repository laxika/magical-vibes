package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;

/**
 * Removes the target creature from combat; the attackers it was blocking that hadn't become blocked by another creature
 * this combat become unblocked; then it blocks an attacking creature its controller can legally have it block, chosen
 * by the effect's controller.
 *
 * <p>Balduvian Warlord: "remove target blocking creature from combat ... then it blocks an attacking creature of your
 * choice" (the default; the target must be blocking). False Orders ({@link #mayReblock()}): "remove target creature
 * defending player controls from combat ... You may have it block an attacking creature of your choice": any creature
 * the spell's target filter allows, and the reblock is asked as a "you may".
 *
 * @param mayReblock whether having it block again is the controller's choice rather than part of the effect
 */
public record BalduvianWarlordEffect(boolean mayReblock) implements CardEffect {

    public BalduvianWarlordEffect() {
        this(false);
    }

    /** "You may have it block an attacking creature of your choice" (False Orders). */
    public static BalduvianWarlordEffect thenMayReblock() {
        return new BalduvianWarlordEffect(true);
    }

    @Override
    public TargetSpec targetSpec() {
        return mayReblock
                ? TargetSpec.benign(TargetPredicates.creature())
                : TargetSpec.benign(TargetPredicates.creature(), new PermanentIsBlockingPredicate());
    }
}
