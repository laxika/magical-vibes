package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "8ED", collectorNumber = "243")
public class ElvishPioneer extends Card {

    public ElvishPioneer() {
        // When this creature enters, you may put a basic land card from your hand onto the battlefield tapped.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new PutCardToBattlefieldEffect(CardPredicateUtils.basicLand(), "basic land", true),
                "Put a basic land card from your hand onto the battlefield tapped?"
        ));
    }
}
