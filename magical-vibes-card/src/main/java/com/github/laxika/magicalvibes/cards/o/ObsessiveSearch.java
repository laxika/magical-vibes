package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "TOR", collectorNumber = "43")
public class ObsessiveSearch extends Card {

    public ObsessiveSearch() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
        addCastingOption(new MadnessCast("{U}"));
    }
}
