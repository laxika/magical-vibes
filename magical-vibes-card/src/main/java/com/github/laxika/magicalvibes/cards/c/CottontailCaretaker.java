package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantOffspringToWhiteCreatureCardInHandEffect;

@CardRegistration(set = "YBLB", collectorNumber = "2")
public class CottontailCaretaker extends Card {

    public CottontailCaretaker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new GrantOffspringToWhiteCreatureCardInHandEffect("{1}"));
    }
}
