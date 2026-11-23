package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToRecipientsEffect;

@CardRegistration(set = "TSP", collectorNumber = "34")
public class PlatedPegasus extends Card {

    public PlatedPegasus() {
        addStaticEffect(PreventDamageToRecipientsEffect.toEveryone().fromSpells().preventing(1));
    }
}
