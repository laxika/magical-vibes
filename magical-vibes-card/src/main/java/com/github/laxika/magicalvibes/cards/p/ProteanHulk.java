package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreaturesWithTotalManaValueAtMostEffect;

@CardRegistration(set = "DIS", collectorNumber = "90")
public class ProteanHulk extends Card {

    public ProteanHulk() {
        addEffect(EffectSlot.ON_DEATH, new SearchLibraryForCreaturesWithTotalManaValueAtMostEffect(6));
    }
}
