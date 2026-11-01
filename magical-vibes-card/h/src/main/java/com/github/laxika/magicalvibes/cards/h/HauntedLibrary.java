package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOC", collectorNumber = "6")
@CardRegistration(set = "VOC", collectorNumber = "44")
public class HauntedLibrary extends Card {

    public HauntedLibrary() {
        // Whenever a creature an opponent controls dies, you may pay {1}. If you do, create a
        // 1/1 white Spirit creature token with flying.
        addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS),
                new MayPayManaEffect("{1}", CreateTokenEffect.whiteSpirit(1),
                        "Pay {1} to create a Spirit token?"));
    }
}
