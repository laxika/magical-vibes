package com.github.laxika.magicalvibes.model.effect;

/**
 * Sets the name of the permanents {@code subject} names: the object loses any names it had and has only
 * {@code name} (CR 612.8), a text-changing effect applied in layer 3 (CR 613.1c).
 *
 * <p>As a static ability (Witness Protection's enchanted creature, Princess Yue) the set is re-evaluated by the
 * layered pass and {@code duration} is unused. Resolved by a spell or ability ("it becomes ... named Vitu-Ghazi",
 * The Curse of Fenric, Mileva) it finds its permanents once (CR 611.2c) and floats a {@link Subjects#target()} copy on
 * each for {@code duration}: a name a resolved ability set is not an ability, so losing all abilities keeps it. Only
 * a {@link Subjects#target()} subject declares a target (benign, any permanent).
 *
 * @param name     the name the affected permanents have
 * @param subject  which permanents are affected
 * @param duration how long a resolved name change lasts
 */
public record SetNameEffect(String name, PermanentSubject subject, Duration duration) implements CardEffect {

    public SetNameEffect(String name, PermanentSubject subject) {
        this(name, subject, Duration.PERMANENT);
    }

    @Override
    public TargetSpec targetSpec() {
        return subject instanceof PermanentSubject.Target
                ? TargetSpec.benign(TargetPredicates.permanent())
                : TargetSpec.NONE;
    }
}
