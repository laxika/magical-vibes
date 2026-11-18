package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandAndApplyPerpetualBlitzEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YSNC", collectorNumber = "26")
public class RiveteersProvocateur extends Card {

    public RiveteersProvocateur() {
        addCastingOption(AlternateHandCast.blitz("{B}{R}"));

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ChooseCardFromHandAndApplyPerpetualBlitzEffect());
    }
}
