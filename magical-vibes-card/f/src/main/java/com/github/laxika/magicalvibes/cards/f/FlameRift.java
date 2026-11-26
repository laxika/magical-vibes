package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "NEM", collectorNumber = "80")
@CardRegistration(set = "MH2", collectorNumber = "278")
public class FlameRift extends Card {

    public FlameRift() {
        addSpellEffect(DealDamageEffect.to(Subjects.eachPlayer(), 4));
    }
}
