package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourceCardPower;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YBRO", collectorNumber = "11")
public class TomakulPhoenix extends Card {

    public TomakulPhoenix() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new PerpetuallyBoostCardEffect(this, 2, 2));
        addTrigger(new TriggerCondition(Triggers.beginningOfCombat(ActivePlayer.YOU), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                new UnlessPaysEffect(Subjects.you(), PayManaCost.withGenericIncrease("{R}", new SourceCardPower()), null,
                        new ReturnSourceCardFromGraveyardToBattlefieldEffect(false),
                        "Pay {X}{R}, where X is Tomakul Phoenix's power, to return it from your graveyard to the battlefield?"));
    }
}
