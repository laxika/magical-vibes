package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardAndBoostSelfIfCreatureEffect;

@CardRegistration(set = "GPT", collectorNumber = "81")
public class Bioplasm extends Card {

    public Bioplasm() {
        addEffect(EffectSlot.ON_ATTACK, new ExileTopCardAndBoostSelfIfCreatureEffect());
    }
}
