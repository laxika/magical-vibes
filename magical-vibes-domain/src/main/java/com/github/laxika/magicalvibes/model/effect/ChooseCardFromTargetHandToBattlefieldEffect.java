package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "You may put a creature card from [defending player's hand] onto the battlefield under your control tapped and
 * attacking. Return that creature to its owner's hand at the beginning of the next end step" (Zara, Renegade
 * Recruiter): reads the defending player's hand from the attack context; nothing is targeted.
 */
public record ChooseCardFromTargetHandToBattlefieldEffect(CardPredicate predicate, String label)
        implements CardEffect {

    /** Non-targeting attack trigger that uses the defending player's hand. */
    public static ChooseCardFromTargetHandToBattlefieldEffect forDefendingPlayerHand(
            CardPredicate predicate, String label) {
        return new ChooseCardFromTargetHandToBattlefieldEffect(predicate, label);
    }
}
