package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BlightEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "210")
public class ChaosSpewer extends Card {

    public ChaosSpewer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayPayManaEffect(
                "{2}",
                null,
                "Pay {2}?",
                new BlightEffect(2, null)));
    }
}
