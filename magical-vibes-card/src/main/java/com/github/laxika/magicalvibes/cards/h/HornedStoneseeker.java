package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "BRO", collectorNumber = "138")
public class HornedStoneseeker extends Card {

    public HornedStoneseeker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofPowerstoneToken(new Fixed(1)));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new SacrificePermanentsEffect(1, new PermanentHasSubtypePredicate(CardSubtype.POWERSTONE),
                        SacrificeRecipient.CONTROLLER));
    }
}
