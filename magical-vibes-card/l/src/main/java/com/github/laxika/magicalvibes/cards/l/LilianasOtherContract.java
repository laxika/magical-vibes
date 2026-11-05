package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceControllerLossWithExileAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB2", collectorNumber = "548")
public class LilianasOtherContract extends Card {

    public LilianasOtherContract() {
        setBackFaceCard(new LilianasUndeadMinion());

        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(3), new LoseLifeEffect(3));
        addStaticEffect(new ReplaceControllerLossWithExileAndReturnTransformedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "LilianasUndeadMinion";
    }
}
