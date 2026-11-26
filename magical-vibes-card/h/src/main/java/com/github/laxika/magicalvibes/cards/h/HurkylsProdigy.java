package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YBRO", collectorNumber = "4")
public class HurkylsProdigy extends Card {

    public HurkylsProdigy() {
        CreateTokenEffect powerstone = CreateTokenEffect.ofPowerstoneToken(new Fixed(1));
        addTrigger(Triggers.enters(ObjectRelation.THIS), powerstone);
        addTrigger(Triggers.attacks(), UnlessPaysEffect.youMayPay("{2}", SequenceEffect.of(
                        MakeCreatureUnblockableEffect.self(),
                        new PerpetuallyBoostCardEffect(this, 2, 0)
                )).withPrompt("Pay {2} to make Hurkyl's Prodigy unblockable this turn and perpetually give it +2/+0?"));
    }
}
