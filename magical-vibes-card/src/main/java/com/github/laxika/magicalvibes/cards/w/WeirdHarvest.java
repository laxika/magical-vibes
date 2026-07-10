package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMaySearchLibraryForCreaturesToHandEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "9ED", collectorNumber = "282")
public class WeirdHarvest extends Card {

    public WeirdHarvest() {
        addEffect(EffectSlot.SPELL, new EachPlayerMaySearchLibraryForCreaturesToHandEffect());
    }
}
