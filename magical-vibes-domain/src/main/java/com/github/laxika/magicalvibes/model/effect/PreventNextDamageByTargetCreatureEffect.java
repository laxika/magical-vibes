package com.github.laxika.magicalvibes.model.effect;

/**
 * "The next time [source] would deal [combat] damage this turn, prevent that damage" — or, {@link #doubling}, "it deals
 * double that damage instead". The source is the target creature (Awe Strike), a creature the trigger was about
 * ({@code triggering()}: Impulsive Maneuvers), or the source a {@link ChooseDamageSourceEffect} chose
 * ({@code thisWay(CHOOSE)}: Desperate Gambit), which may be a spell (CR 609.7a). The shield applies to the whole next
 * damage event that source deals, to any recipient; prevention does nothing to damage that can't be prevented and is
 * not used up by it (CR 615.12), while doubling is a replacement and applies regardless.
 *
 * @param source     whose next damage: {@code target()} declares a benign creature target
 * @param gainLife   whether the controller gains life equal to the damage prevented
 * @param doubling   whether the damage is doubled instead of prevented
 * @param combatOnly whether only combat damage uses the shield
 */
public record PreventNextDamageByTargetCreatureEffect(PermanentSubject source, boolean gainLife, boolean doubling,
                                                      boolean combatOnly) implements CardEffect, DoublingEffect {

    public PreventNextDamageByTargetCreatureEffect(boolean gainLife) {
        this(Subjects.target(), gainLife, false, false);
    }

    public PreventNextDamageByTargetCreatureEffect() {
        this(true);
    }

    /** "The next time [source] would deal damage this turn, prevent that damage." */
    public static PreventNextDamageByTargetCreatureEffect preventing(PermanentSubject source) {
        return new PreventNextDamageByTargetCreatureEffect(source, false, false, false);
    }

    /** "The next time [source] would deal damage this turn, it deals double that damage instead." */
    public static PreventNextDamageByTargetCreatureEffect doubling(PermanentSubject source) {
        return new PreventNextDamageByTargetCreatureEffect(source, false, true, false);
    }

    /** This effect, used only by the source's next combat damage ("the next time it would deal combat damage"). */
    public PreventNextDamageByTargetCreatureEffect combatDamageOnly() {
        return new PreventNextDamageByTargetCreatureEffect(source, gainLife, doubling, true);
    }

    @Override
    public boolean isDoublingEffect() {
        return doubling;
    }

    @Override
    public TargetSpec targetSpec() {
        return source.declaresTarget() ? TargetSpec.benign(TargetPredicates.creature()) : TargetSpec.NONE;
    }
}
