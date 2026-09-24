package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToSelfFromCreaturesEffect;

@CardRegistration(set = "S99", collectorNumber = "11")
@CardRegistration(set = "ME4", collectorNumber = "8")
public class ChampionLancer extends Card {

    public ChampionLancer() {
        addEffect(EffectSlot.STATIC, new PreventDamageToSelfFromCreaturesEffect());
    }
}
