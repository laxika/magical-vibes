package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "161")
public class GolemsHeart extends Card {

    public GolemsHeart() {
        addTrigger(Triggers.playerCastsSpell().withFilter(new CardTypePredicate(CardType.ARTIFACT)),
                new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
    }
}
