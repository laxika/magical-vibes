package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "7")
@CardRegistration(set = "TPR", collectorNumber = "6")
public class AvengingAngel extends Card {

    public AvengingAngel() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(
                new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(0),
                "Put Avenging Angel on top of its owner's library?"));
    }
}
