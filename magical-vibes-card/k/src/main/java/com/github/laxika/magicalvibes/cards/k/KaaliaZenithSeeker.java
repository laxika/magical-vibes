package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsRevealTwoTypesToHandThenRestEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "210")
public class KaaliaZenithSeeker extends Card {

    public KaaliaZenithSeeker() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                LookAtTopCardsRevealTwoTypesToHandThenRestEffect.angelDemonDragonToHandRestOnBottom(6));
    }
}
