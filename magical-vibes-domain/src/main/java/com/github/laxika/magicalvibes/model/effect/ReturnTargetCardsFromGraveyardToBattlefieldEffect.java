package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Returns targeted cards from the controller's graveyard to the battlefield.
 *
 * <p>The one-argument form returns exactly the spell's paid X cards. The fixed-cap form returns up
 * to {@code maxTargets} cards and can restrict them to cards put into the graveyard from the
 * battlefield this turn.</p>
 */
public record ReturnTargetCardsFromGraveyardToBattlefieldEffect(
        CardPredicate filter,
        int maxTargets,
        boolean fromBattlefieldThisTurn,
        boolean enterTapped
) implements CardEffect {

    /** Creates the X-scaled form used by Return to the Ranks. */
    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter) {
        this(filter, 0, false, false);
    }

    /** Creates the fixed-cap form used by up-to-N reanimation spells. */
    public ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter, int maxTargets,
                                                              boolean fromBattlefieldThisTurn,
                                                              boolean enterTapped) {
        if (maxTargets < 0) {
            throw new IllegalArgumentException("maxTargets cannot be negative");
        }
        this.filter = filter;
        this.maxTargets = maxTargets;
        this.fromBattlefieldThisTurn = fromBattlefieldThisTurn;
        this.enterTapped = enterTapped;
    }

    public boolean xScaled() {
        return maxTargets == 0;
    }
}
