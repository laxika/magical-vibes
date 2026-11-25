package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "KLD", collectorNumber = "80")
@CardRegistration(set = "KLR", collectorNumber = "83")
public class EssenceExtraction extends Card {

    public EssenceExtraction() {
        addSpellEffect(DealDamageEffect.toTargetCreature(3));
        addSpellEffect(new GainLifeEffect(3));
    }
}
