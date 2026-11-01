package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHR", collectorNumber = "72")
@CardRegistration(set = "LEG", collectorNumber = "219")
@CardRegistration(set = "ME3", collectorNumber = "143")
public class AxelrodGunnarson extends Card {

    public AxelrodGunnarson() {
        addTrigger(Triggers.creatureDies(ObjectRelation.DAMAGED_BY_THIS), SequenceEffect.of(
                new GainLifeEffect(1),
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)));
    }
}
