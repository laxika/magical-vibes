package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "233")
@CardRegistration(set = "DBL", collectorNumber = "500")
public class BrineComber extends Card {

    public BrineComber() {
        setBackFaceCard(new BrineboundGift());

        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.whiteSpirit(1));
        addTrigger(Triggers.becomesTargetOfAuraSpell(), CreateTokenEffect.whiteSpirit(1));
        addCastingOption(new DisturbCast("{W}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "BrineboundGift";
    }
}
