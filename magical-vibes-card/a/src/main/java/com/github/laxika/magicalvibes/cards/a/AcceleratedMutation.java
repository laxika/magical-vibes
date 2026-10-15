package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "SCG", collectorNumber = "109")
public class AcceleratedMutation extends Card {

    public AcceleratedMutation() {
        GreatestManaValueAmongControlled greatestManaValue =
                new GreatestManaValueAmongControlled(new PermanentTruePredicate());
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(greatestManaValue, greatestManaValue));
    }
}
