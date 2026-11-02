package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "POR", collectorNumber = "189")
public class ThunderingWurm extends Card {

    public ThunderingWurm() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SacrificeUnlessDiscardCardTypeEffect(CardType.LAND));
    }
}
