package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LEG", collectorNumber = "27")
public class Lifeblood extends Card {

    public Lifeblood() {
        // Whenever a Mountain an opponent controls becomes tapped, you gain 1 life.
        addEffect(EffectSlot.ON_OPPONENT_PERMANENT_BECOMES_TAPPED, new TriggeringPermanentConditionalEffect(
                new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN),
                new GainLifeEffect(1)));
    }
}
