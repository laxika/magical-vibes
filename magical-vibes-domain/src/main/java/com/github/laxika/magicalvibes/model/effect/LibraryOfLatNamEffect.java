package com.github.laxika.magicalvibes.model.effect;

/**
 * Library of Lat-Nam. An opponent chooses one — either you draw three cards at the beginning of the
 * next turn's upkeep, or you search your library for a card, put that card into your hand, then
 * shuffle. Per CR 700.2e the opponent chooses the mode while the spell is cast, through the
 * may-ability (accept/decline) system; the choice is recorded on the spell as {@code drawThree}
 * (accept = draw three, decline = search) and carried out on resolution.
 *
 * @param drawThree the mode the opponent chose, or {@code null} while no choice has been made
 */
public record LibraryOfLatNamEffect(Boolean drawThree) implements CardEffect {

    public LibraryOfLatNamEffect() {
        this(null);
    }
}
