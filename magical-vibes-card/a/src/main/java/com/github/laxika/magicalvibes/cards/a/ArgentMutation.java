package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AddCardTypeToTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "NPH", collectorNumber = "27")
public class ArgentMutation extends Card {

    public ArgentMutation() {
        addSpellEffect(new AddCardTypeToTargetPermanentEffect(CardType.ARTIFACT));
        addSpellEffect(new DrawCardEffect());
    }
}
