package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAnyNumberOfOwnGraveyardCardsThenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import static com.github.laxika.magicalvibes.model.CardType.CREATURE;

@CardRegistration(set = "WOE", collectorNumber = "107")
public class SpecterOfMortality extends Card {

    public SpecterOfMortality() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileAnyNumberOfOwnGraveyardCardsThenEffect(new CardTypePredicate(CREATURE),
                        new BoostAllCreaturesEffect(new Scaled(new EventValue(), -1),
                                new Scaled(new EventValue(), -1),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))));
    }
}
