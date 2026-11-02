package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileAnyGraveyardCardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForEachSharedCardTypeWithExiledCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "191")
@CardRegistration(set = "DBL", collectorNumber = "458")
public class CemeteryProwler extends Card {

    public CemeteryProwler() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ExileAnyGraveyardCardAndImprintOnSourceEffect());
        addTrigger(Triggers.attacks(), new ExileAnyGraveyardCardAndImprintOnSourceEffect());
        addStaticEffect(new ReduceOwnCastCostForEachSharedCardTypeWithExiledCardsEffect());
    }
}
