package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * Searches the controller's library for land cards, reveals them, puts the requested number onto
 * the battlefield tapped and the remainder into the controller's hand, then shuffles.
 *
 * @param subtype             when non-null, only cards with this subtype qualify
 * @param extraCardCondition  when non-null and met as the effect resolves, one additional card is
 *                            found and put into the controller's hand (Nissa's Pilgrimage's spell
 *                            mastery rider searches for up to three cards instead of two)
 * @param basicOnly           when true, only basic land cards qualify; when false, cards with the
 *                            requested subtype qualify even if they are nonbasic (Flourishing
 *                            Bloom-Kin searches for Forest cards)
 * @param battlefieldCount    number of cards that may be found for the battlefield portion; the
 *                            normal Cultivate-style flow uses one, while Viewpoint Synchronization
 *                            uses two and puts a third found card into hand
 */
public record SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect(CardSubtype subtype,
                                                                        Condition extraCardCondition,
                                                                        boolean basicOnly,
                                                                        int battlefieldCount)
        implements CardEffect {

    public SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect {
        if (battlefieldCount < 1) {
            throw new IllegalArgumentException("Battlefield land count must be positive");
        }
    }

    public SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect(CardSubtype subtype,
                                                                       Condition extraCardCondition,
                                                                       boolean basicOnly) {
        this(subtype, extraCardCondition, basicOnly, 1);
    }

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

    /** Up to three basic lands: two to the battlefield tapped and the third into hand (Viewpoint Synchronization). */
    public static SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect twoToBattlefieldTapped() {
        return new SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect(null, null, true, 2);
    }
}
