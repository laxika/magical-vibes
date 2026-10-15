package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LCI", collectorNumber = "183")
@CardRegistration(set = "LCI", collectorNumber = "325")
public class EarthshakerDreadmaw extends Card {

    public EarthshakerDreadmaw() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR), CountScope.CONTROLLER, true)));
    }
}
