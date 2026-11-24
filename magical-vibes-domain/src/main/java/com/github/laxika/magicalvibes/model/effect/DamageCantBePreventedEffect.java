package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "[combat] damage [that would be dealt by sources] can't be prevented" (CR 615.12). Prevention effects
 * still apply to such damage for their other effects, but prevent none of it and use up no shield.
 *
 * <ul>
 *   <li>{@code ()}: "Damage can't be prevented" (Leyline of Punishment, Everlasting Torment, Sunspine Lynx,
 *       Spider-Punk).</li>
 *   <li>{@link #combatDamage()}: "Combat damage can't be prevented" (Frenzied Baloth).</li>
 *   <li>{@link #dealtBySource()}: "Damage that would be dealt by this creature can't be prevented" (Malignus,
 *       Excruciator).</li>
 *   <li>{@link #combatDamageDealtBy(PermanentSubject)} over {@code controlledBy(you(), creature)}: "Combat damage that
 *       would be dealt by creatures you control can't be prevented" (Questing Beast, and the Questing Role's creature
 *       that is granted it).</li>
 * </ul>
 *
 * @param sources    the permanents whose damage can't be prevented, {@code source()} or a set the holder's controller
 *                   controls ({@code controlledBy(you(), filter)}); {@code null} for damage from every source
 * @param combatOnly whether only combat damage can't be prevented
 */
public record DamageCantBePreventedEffect(PermanentSubject sources, boolean combatOnly) implements CardEffect {

    public DamageCantBePreventedEffect {
        if (sources != null && !(sources instanceof PermanentSubject.Source)
                && !(sources instanceof PermanentSubject.ControlledBy controlled
                && controlled.players() instanceof PlayerSubject.You)) {
            throw new IllegalArgumentException("Sources are this permanent or permanents you control: " + sources);
        }
    }

    /** "Damage can't be prevented." */
    public DamageCantBePreventedEffect() {
        this(null, false);
    }

    /** "Combat damage can't be prevented." */
    public static DamageCantBePreventedEffect combatDamage() {
        return new DamageCantBePreventedEffect(null, true);
    }

    /** "Damage that would be dealt by this creature can't be prevented." */
    public static DamageCantBePreventedEffect dealtBySource() {
        return new DamageCantBePreventedEffect(Subjects.source(), false);
    }

    /** "Combat damage that would be dealt by [sources] can't be prevented." */
    public static DamageCantBePreventedEffect combatDamageDealtBy(PermanentSubject sources) {
        return new DamageCantBePreventedEffect(sources, true);
    }
}
