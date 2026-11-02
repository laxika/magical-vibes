package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileRandomPermanentFromGraveyardCreateTappedTokenCopyEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "242")
@CardRegistration(set = "FIN", collectorNumber = "348")
@CardRegistration(set = "FIN", collectorNumber = "508")
public class SinSpirasPunishment extends Card {

    public SinSpirasPunishment() {
        var effect = new ExileRandomPermanentFromGraveyardCreateTappedTokenCopyEffect();
        addTrigger(Triggers.enters(ObjectRelation.THIS), effect);
        addTrigger(Triggers.attacks(), effect);
    }
}
