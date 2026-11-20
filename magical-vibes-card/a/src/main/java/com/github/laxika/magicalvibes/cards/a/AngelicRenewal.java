package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "WTH", collectorNumber = "4")
@CardRegistration(set = "UMA", collectorNumber = "10")
public class AngelicRenewal extends Card {

    public AngelicRenewal() {
        // Whenever a creature is put into your graveyard from the battlefield, you may sacrifice
        // this enchantment. If you do, return that card to the battlefield.
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_OWN),
                new MayEffect(SequenceEffect.of(new SacrificeSelfEffect(),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), new ReturnTriggeringCardFromGraveyardToBattlefieldEffect(false, true))),
                        "Sacrifice Angelic Renewal to return the creature to the battlefield?"));
    }
}
