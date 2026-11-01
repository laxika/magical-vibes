package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WTH", collectorNumber = "4")
@CardRegistration(set = "UMA", collectorNumber = "10")
public class AngelicRenewal extends Card {

    public AngelicRenewal() {
        // Whenever a creature is put into your graveyard from the battlefield, you may sacrifice
        // this enchantment. If you do, return that card to the battlefield.
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_OWN),
                new MayEffect(new SacrificeSelfThenEffect(new ReturnTriggeringCreatureToBattlefieldEffect()),
                        "Sacrifice Angelic Renewal to return the creature to the battlefield?"));
    }
}
