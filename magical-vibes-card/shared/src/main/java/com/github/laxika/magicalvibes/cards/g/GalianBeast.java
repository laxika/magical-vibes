package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/** Back face of {@link com.github.laxika.magicalvibes.cards.v.VincentValentine}. */
public class GalianBeast extends Card {

    public GalianBeast() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ReturnSourceCardFromGraveyardToBattlefieldEffect(true));
    }
}
