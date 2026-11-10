package com.github.laxika.magicalvibes.model.trigger;

/**
 * A Saga's chapter ability (CR 715.2): "{rN}—[Effect]" means "When one or more lore counters are put onto this Saga,
 * if the number of lore counters on it was less than N and became at least N, [effect]" (CR 715.2b). A card has at
 * most one chapter ability for each chapter number, so {@code Card.addTrigger} adds every effect of one chapter to the
 * same ability; "{rN1}, {rN2}—[Effect]" is two chapter abilities (CR 715.2c). Build it through {@link Triggers}.
 *
 * @param chapter the chapter number N, 1 for chapter I
 */
public record ChapterTrigger(int chapter) implements Trigger {

    private static final String[] NUMERALS = {"I", "II", "III", "IV", "V", "VI"};

    public ChapterTrigger {
        if (chapter < 1) {
            throw new IllegalArgumentException("A chapter number is at least 1, got " + chapter);
        }
    }

    /** The chapter's Roman numeral, as printed (CR 715.2a): "III" for chapter 3. */
    public String numeral() {
        return chapter <= NUMERALS.length ? NUMERALS[chapter - 1] : String.valueOf(chapter);
    }
}
