package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "136")
@CardRegistration(set = "SLD", collectorNumber = "1409")
@CardRegistration(set = "MUL", collectorNumber = "20")
@CardRegistration(set = "MUL", collectorNumber = "85")
@CardRegistration(set = "MUL", collectorNumber = "150")
@CardRegistration(set = "FCA", collectorNumber = "38")
@CardRegistration(set = "OTC", collectorNumber = "158")
@CardRegistration(set = "LCC", collectorNumber = "218")
@CardRegistration(set = "DMC", collectorNumber = "120")
public class CaptainLanneryStorm extends Card {

    public CaptainLanneryStorm() {
        addTrigger(Triggers.attacks(), CreateTokenEffect.ofTreasureToken(1));
        addTrigger(Triggers.sacrificed(
                        new PermanentHasSubtypePredicate(CardSubtype.TREASURE),
                        ObjectRelation.YOU_CONTROL),
                new BoostSelfEffect(1, 0));
    }
}
