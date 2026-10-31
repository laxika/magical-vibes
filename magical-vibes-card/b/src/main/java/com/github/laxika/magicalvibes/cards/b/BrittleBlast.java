package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantExileInsteadOfDyingEffect;

@CardRegistration(set = "YMID", collectorNumber = "37")
public class BrittleBlast extends Card {

    public BrittleBlast() {
        addSpellEffect(new PerpetuallyGrantExileInsteadOfDyingEffect());
        addSpellEffect(new DealDamageToTargetCreatureOrPlaneswalkerEffect(5));
    }
}
