package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalUpkeepStepEffect;

@CardRegistration(set = "TSP", collectorNumber = "71")
public class ParadoxHaze extends Card {

    public ParadoxHaze() {
        setEnchantPlayer(true);
        addEffect(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED, new AdditionalUpkeepStepEffect());
    }
}
