package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReplaceCreatureTokenCreationEffect;

@CardRegistration(set = "GRN", collectorNumber = "10")
@CardRegistration(set = "2X2", collectorNumber = "8")
public class DivineVisitation extends Card {

    public DivineVisitation() {
        addEffect(EffectSlot.STATIC, new ReplaceCreatureTokenCreationEffect());
    }
}
