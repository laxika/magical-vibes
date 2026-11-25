package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "DFT", collectorNumber = "192")
public class BroadsideBarrage extends Card {

    public BroadsideBarrage() {
        addSpellEffect(DealDamageEffect.toTargetCreatureOrPlaneswalker(5));
        addSpellEffect(new DrawCardEffect(1));
        addSpellEffect(new DiscardEffect(1, Subjects.you()));
    }
}
