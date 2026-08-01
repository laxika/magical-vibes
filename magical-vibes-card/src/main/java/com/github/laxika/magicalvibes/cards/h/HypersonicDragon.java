package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "RTR", collectorNumber = "170")
public class HypersonicDragon extends Card {

    public HypersonicDragon() {
        // Flying and haste are auto-loaded from Scryfall keywords.
        addEffect(EffectSlot.STATIC, new GrantFlashToCardTypeEffect(new CardTypePredicate(CardType.SORCERY)));
    }
}
