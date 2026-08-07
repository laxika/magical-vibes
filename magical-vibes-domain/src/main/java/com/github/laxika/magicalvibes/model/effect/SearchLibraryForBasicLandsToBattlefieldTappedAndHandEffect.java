package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * Searches the controller's library for basic land cards, reveals them, puts one onto the
 * battlefield tapped and the rest into the controller's hand, then shuffles.
 *
 * @param subtype             when non-null, only basic lands with this subtype qualify
 *                            (Nissa's Pilgrimage searches for basic Forest cards)
 * @param extraCardCondition  when non-null and met as the effect resolves, one additional card is
 *                            found and put into the controller's hand (Nissa's Pilgrimage's spell
 *                            mastery rider searches for up to three cards instead of two)
 */
public record SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect(CardSubtype subtype,
                                                                        Condition extraCardCondition)
        implements CardEffect {

    /** Up to two basic land cards of any subtype: one to the battlefield tapped, one to hand (Cultivate). */
    public SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect() {
        this(null, null);
    }
}
