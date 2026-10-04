package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseUpToOneCreatureDestroyRestEffect;

@CardRegistration(set = "KTK", collectorNumber = "174")
@CardRegistration(set = "DMC", collectorNumber = "148")
@CardRegistration(set = "C20", collectorNumber = "211")
@CardRegistration(set = "C16", collectorNumber = "194")
@CardRegistration(set = "CM2", collectorNumber = "154")
public class Duneblast extends Card {

    public Duneblast() {
        addEffect(EffectSlot.SPELL, new ChooseUpToOneCreatureDestroyRestEffect());
    }
}
