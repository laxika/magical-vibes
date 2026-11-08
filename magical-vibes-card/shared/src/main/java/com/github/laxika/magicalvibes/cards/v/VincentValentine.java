package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GalianBeast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentPower;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "125")
@CardRegistration(set = "FIN", collectorNumber = "383")
@CardRegistration(set = "FIN", collectorNumber = "454")
@CardRegistration(set = "FIN", collectorNumber = "528")
public class VincentValentine extends Card {

    public VincentValentine() {
        setBackFaceCard(new GalianBeast());

        addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new TriggeringPermanentPower()));
        addTrigger(Triggers.attacks(),
                new MayEffect(new TransformSelfEffect(), "Transform Vincent Valentine?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "GalianBeast";
    }
}
