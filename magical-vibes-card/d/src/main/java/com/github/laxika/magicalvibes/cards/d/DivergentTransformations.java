package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;


@CardRegistration(set = "CMM", collectorNumber = "216")
@CardRegistration(set = "CMM", collectorNumber = "534")
@CardRegistration(set = "C16", collectorNumber = "17")
public class DivergentTransformations extends Card {

    public DivergentTransformations() {
        addStaticEffect(new ReduceOwnCastCostEffect(new Sum(new PlayersInGame(), new Fixed(-1))));

        target(TargetFilters.creature(), 2, 2)
                .addSpellEffect(new ExileTargetPermanentEffect())
                .addSpellEffect(RevealUntilCardPredicateRestOnBottomRandomEffect.ontoBattlefield(
                        new CardTypePredicate(CardType.CREATURE),
                        new RevealUntilCardPredicateRestOnBottomRandomEffect.ControllerOfEachExiledThisWay()).shufflingRest());
    }
}
