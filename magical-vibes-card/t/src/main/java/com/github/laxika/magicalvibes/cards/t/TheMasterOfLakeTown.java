package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.GraveyardsAtLeast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillOpponentOnLifeLossEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOB", collectorNumber = "77")
public class TheMasterOfLakeTown extends Card {

    public TheMasterOfLakeTown() {
        MillOpponentOnLifeLossEffect millOnLifeLoss = new MillOpponentOnLifeLossEffect();
        addTrigger(Triggers.opponentLosesLife(), millOnLifeLoss);
        addTrigger(Triggers.youLoseLife(), millOnLifeLoss);
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DrawCardEffect(new GraveyardsAtLeast(7)));
    }
}
