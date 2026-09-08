package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * Reveals the top card of the controller's library. If it matches {@code predicate}, put it onto
 * the battlefield. Otherwise, put it into the controller's graveyard.
 *
 * <p>When {@code grantHaste} is true the entering creature gains haste until end of turn; when
 * {@code sacrificeAtEndStep} is true it is sacrificed at the beginning of the next end step
 * (Impromptu Raid). Both false for the plain Call of the Wild variant.
 */
public record RevealTopCardCreatureToBattlefieldElseGraveyardEffect(CardPredicate predicate,
                                                                    boolean grantHaste,
                                                                    boolean sacrificeAtEndStep)
        implements CardEffect {

    public RevealTopCardCreatureToBattlefieldElseGraveyardEffect() {
        this(new CardTypePredicate(CardType.CREATURE), false, false);
    }

    public RevealTopCardCreatureToBattlefieldElseGraveyardEffect(boolean grantHaste,
                                                                  boolean sacrificeAtEndStep) {
        this(new CardTypePredicate(CardType.CREATURE), grantHaste, sacrificeAtEndStep);
    }

    public RevealTopCardCreatureToBattlefieldElseGraveyardEffect(CardPredicate predicate) {
        this(predicate, false, false);
    }
}
