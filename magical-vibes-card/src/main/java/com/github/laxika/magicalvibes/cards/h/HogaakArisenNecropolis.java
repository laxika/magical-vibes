package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.effect.DelveCost;

@CardRegistration(set = "MH1", collectorNumber = "202")
@CardRegistration(set = "MB2", collectorNumber = "136")
public class HogaakArisenNecropolis extends Card {

    public HogaakArisenNecropolis() {
        setRequiresNoMana(true);
        addEffect(EffectSlot.SPELL, new DelveCost());
        addCastingOption(new GraveyardCast());
    }
}
