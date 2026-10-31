package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "RAV", collectorNumber = "7")
@CardRegistration(set = "MOC", collectorNumber = "177")
public class ChantOfVituGhazi extends Card {

    public ChantOfVituGhazi() {
        addSpellEffect(PreventDamageEffect.allByCreaturesAndGainLife());
    }
}
