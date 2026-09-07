package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * Searches the controller's library for land cards, reveals them, puts one onto the battlefield
 * tapped and the rest into the controller's hand, then shuffles.
 *
 * @param subtype             when non-null, only cards with this subtype qualify
 * @param extraCardCondition  when non-null and met as the effect resolves, one additional card is
 *                            found and put into the controller's hand (Nissa's Pilgrimage's spell
 *                            mastery rider searches for up to three cards instead of two)
 * @param basicOnly           when true, only basic land cards qualify; when false, cards with the
 *                            requested subtype qualify even if they are nonbasic (Flourishing
 *                            Bloom-Kin searches for Forest cards)
 */
public record SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect(CardSubtype subtype,
                                                                        Condition extraCardCondition,
                                                                        boolean basicOnly)
        implements CardEffect {

    /** Up to two basic land cards of any subtype: one to the battlefield tapped, one to hand (Cultivate). */
    public SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect() {
        this(null, null, true);
    }

    public SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect(CardSubtype subtype,
                                                                       Condition extraCardCondition) {
        this(subtype, extraCardCondition, true);
    }

    /** Up to two cards with the given land subtype: one to the battlefield tapped, one to hand. */
    public static SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect landSubtype(
            CardSubtype subtype) {
        return new SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect(subtype, null, false);
    }
}
