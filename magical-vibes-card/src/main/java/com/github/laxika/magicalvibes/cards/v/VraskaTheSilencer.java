package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingOpponentCreatureAsTreasureEffect;

@CardRegistration(set = "OTJ", collectorNumber = "237")
public class VraskaTheSilencer extends Card {

    public VraskaTheSilencer() {
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES,
                new MayPayManaEffect("{1}", new ReturnDyingOpponentCreatureAsTreasureEffect(),
                        "Pay {1} to return that creature to the battlefield under your control as a Treasure?"));
    }
}
