package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;

@CardRegistration(set = "IKO", collectorNumber = "225")
public class KerugaTheMacrosage extends Card {

    public KerugaTheMacrosage() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(
                new PermanentCount(new PermanentMinManaValuePredicate(3), CountScope.CONTROLLER, true)));
    }
}
