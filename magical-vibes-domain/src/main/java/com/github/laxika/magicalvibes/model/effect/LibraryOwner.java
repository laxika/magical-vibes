package com.github.laxika.magicalvibes.model.effect;

/**
 * Whose library a top-of-library condition ({@code TopCardOfLibraryType}, {@code TopCardOfLibraryMatchesPredicate})
 * looks at. Effects name the player with a {@link PlayerSubject} instead; conditions keep this enum until they can
 * resolve a subject.
 *
 * <ul>
 *   <li>{@link #CONTROLLER} — "your library", the condition's controller.</li>
 *   <li>{@link #ENCHANTED_PERMANENT_CONTROLLER} — the player an enchanted-permanent-controller upkeep trigger recorded
 *       on its stack entry (Paroxysm's "that player"; the same player its reveal reads through
 *       {@code Subjects.triggeringPlayer()}).</li>
 * </ul>
 */
public enum LibraryOwner {
    CONTROLLER,
    ENCHANTED_PERMANENT_CONTROLLER
}
