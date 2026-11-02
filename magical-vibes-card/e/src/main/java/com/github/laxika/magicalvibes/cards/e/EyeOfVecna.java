package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "243")
public class EyeOfVecna extends Card {

    public EyeOfVecna() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(2)));

        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new MayPayManaEffect("{2}",
                        SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(2)),
                        "Pay {2} to draw a card?"));
    }
}
