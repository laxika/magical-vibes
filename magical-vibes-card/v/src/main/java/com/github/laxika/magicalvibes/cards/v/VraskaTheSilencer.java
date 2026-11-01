package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingOpponentCreatureAsTreasureEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "237")
public class VraskaTheSilencer extends Card {

    public VraskaTheSilencer() {
        addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS),
                new MayPayManaEffect("{1}", new ReturnDyingOpponentCreatureAsTreasureEffect(),
                        "Pay {1} to return that creature to the battlefield under your control as a Treasure?"));
    }
}
