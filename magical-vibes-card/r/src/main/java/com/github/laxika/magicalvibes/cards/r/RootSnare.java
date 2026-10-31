package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "M19", collectorNumber = "199")
@CardRegistration(set = "RNA", collectorNumber = "137")
public class RootSnare extends Card {

    public RootSnare() {
        addSpellEffect(PreventDamageEffect.allCombat());
    }
}
