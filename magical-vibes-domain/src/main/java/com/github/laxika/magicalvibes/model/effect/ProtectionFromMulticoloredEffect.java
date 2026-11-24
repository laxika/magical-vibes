package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants protection from the sources with a number of colors (CR 105.2): {@link ColorCount#MULTICOLORED} two or more
 * (the no-argument form; Enemy of the Guildpact), {@link ColorCount#MONOCOLORED} exactly one ({@link #monocolored()};
 * Guardian of the Guildpact), {@link ColorCount#ENEMY_COLORED_PAIR} exactly two enemy colors
 * ({@link #enemyColoredMulticolored()}; Frenemy of the Guildpact), {@link ColorCount#COLORLESS} none
 * ({@link #colorless()}; the colorless answer to a "protection from the color of your choice or from colorless"
 * prompt). The source's colors are its current ones.
 *
 * @param subject {@code null} means the permanent itself has protection;
 *                {@link Subjects#attachedCreature()} means the enchanted creature has it
 * @param colors  the number of colors protected from
 */
public record ProtectionFromMulticoloredEffect(PermanentSubject subject, ColorCount colors)
        implements ProtectionGrantingEffect {

    /** How many colors a source must have to be protected from. */
    public enum ColorCount {
        MULTICOLORED, MONOCOLORED, ENEMY_COLORED_PAIR, COLORLESS
    }

    /** "Protection from multicolored". */
    public ProtectionFromMulticoloredEffect() {
        this(null);
    }

    /** "Protection from multicolored" for {@code subject} ({@code null}: the permanent itself). */
    public ProtectionFromMulticoloredEffect(PermanentSubject subject) {
        this(subject, ColorCount.MULTICOLORED);
    }

    /** "Protection from monocolored". */
    public static ProtectionFromMulticoloredEffect monocolored() {
        return new ProtectionFromMulticoloredEffect(null, ColorCount.MONOCOLORED);
    }

    /** "Protection from enemy-colored multicolored": sources with exactly two enemy colors. */
    public static ProtectionFromMulticoloredEffect enemyColoredMulticolored() {
        return new ProtectionFromMulticoloredEffect(null, ColorCount.ENEMY_COLORED_PAIR);
    }

    /** "Protection from colorless": every source that has no color. */
    public static ProtectionFromMulticoloredEffect colorless() {
        return new ProtectionFromMulticoloredEffect(null, ColorCount.COLORLESS);
    }

    @Override
    public boolean protectionFromMulticolored() {
        return colors == ColorCount.MULTICOLORED;
    }

    @Override
    public boolean protectionFromMonocolored() {
        return colors == ColorCount.MONOCOLORED;
    }

    @Override
    public boolean protectionFromEnemyColoredMulticolored() {
        return colors == ColorCount.ENEMY_COLORED_PAIR;
    }

    @Override
    public boolean protectionFromColorless() {
        return colors == ColorCount.COLORLESS;
    }

    @Override
    public PermanentSubject protectionSubject() {
        return subject;
    }
}
