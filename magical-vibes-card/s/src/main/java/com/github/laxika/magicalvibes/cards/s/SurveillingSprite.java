package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "67")
@CardRegistration(set = "DDH", collectorNumber = "43")
public class SurveillingSprite extends Card {

    public SurveillingSprite() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(new DrawCardEffect(1), "Draw a card?"));
    }
}
