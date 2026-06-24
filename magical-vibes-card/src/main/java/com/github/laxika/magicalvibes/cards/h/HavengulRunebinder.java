package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "39")
public class HavengulRunebinder extends Card {

    public HavengulRunebinder() {
        // {2}{U}, {T}, Exile a creature card from your graveyard: Create a 2/2 black Zombie
        // creature token, then put a +1/+1 counter on each Zombie creature you control.
        addActivatedAbility(new ActivatedAbility(
                true, "{2}{U}",
                List.of(
                        new ExileCardFromGraveyardCost(CardType.CREATURE),
                        CreateTokenEffect.blackZombie(1),
                        new PutPlusOnePlusOneCounterOnEachControlledPermanentEffect(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE),
                                        new PermanentIsCreaturePredicate()
                                ))
                        )
                ),
                "{2}{U}, {T}, Exile a creature card from your graveyard: Create a 2/2 black Zombie creature token, then put a +1/+1 counter on each Zombie creature you control."
        ));
    }
}
