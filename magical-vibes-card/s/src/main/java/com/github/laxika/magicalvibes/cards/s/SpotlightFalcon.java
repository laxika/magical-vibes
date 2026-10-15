package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CollectEvidenceCostPaid;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceCost;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

@CardRegistration(set = "YMKM", collectorNumber = "3")
public class SpotlightFalcon extends Card {

    public SpotlightFalcon() {
        addEffect(EffectSlot.SPELL, new CollectEvidenceCost(5, true));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new CollectEvidenceCostPaid(), new ConjureCardToHandEffect("Spotlight Falcon")));
    }
}
