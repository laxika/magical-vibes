package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "TMT", collectorNumber = "93")
@CardRegistration(set = "TMT", collectorNumber = "236")
public class JennikasTechnique extends Card {

    public JennikasTechnique() {
        addSneak("{R}");
        addSpellEffect(DealDamageEffect.toEachCreature(2));
    }
}
