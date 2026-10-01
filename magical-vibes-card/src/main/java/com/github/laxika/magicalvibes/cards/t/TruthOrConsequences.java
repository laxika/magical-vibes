package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TruthOrConsequencesEffect;

@CardRegistration(set = "WHO", collectorNumber = "163")
@CardRegistration(set = "WHO", collectorNumber = "768")
public class TruthOrConsequences extends Card {

    public TruthOrConsequences() {
        addEffect(EffectSlot.SPELL, new TruthOrConsequencesEffect());
    }
}
