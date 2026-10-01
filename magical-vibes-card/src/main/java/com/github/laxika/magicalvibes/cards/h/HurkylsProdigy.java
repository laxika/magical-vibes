package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "YBRO", collectorNumber = "4")
public class HurkylsProdigy extends Card {

    public HurkylsProdigy() {
        CreateTokenEffect powerstone = CreateTokenEffect.ofPowerstoneToken(new Fixed(1));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, powerstone);
        addEffect(EffectSlot.ON_ATTACK, new MayPayManaEffect(
                "{2}",
                SequenceEffect.of(
                        new MakeCreatureUnblockableEffect(true),
                        new PerpetuallyBoostSourceEffect(2, 0)
                ),
                "Pay {2} to make Hurkyl's Prodigy unblockable this turn and perpetually give it +2/+0?"
        ));
    }
}
