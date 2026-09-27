package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The defending player chooses one matching card from the ability controller's graveyard and
 * puts it onto the battlefield under the ability controller's control, optionally with the
 * effect's reanimation riders.
 */
public record DefendingPlayerChoosesCardFromGraveyardToBattlefieldEffect(
        CardPredicate filter,
        CardSubtype grantSubtype,
        CounterType enterCounter)
        implements CardEffect {

    public DefendingPlayerChoosesCardFromGraveyardToBattlefieldEffect(CardPredicate filter) {
        this(filter, CardSubtype.VAMPIRE, CounterType.PLUS_ONE_PLUS_ONE);
    }

    public static DefendingPlayerChoosesCardFromGraveyardToBattlefieldEffect withoutRiders(
            CardPredicate filter) {
        return new DefendingPlayerChoosesCardFromGraveyardToBattlefieldEffect(filter, null, null);
    }
}
