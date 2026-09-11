package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

@CardRegistration(set = "TMT", collectorNumber = "80")
@CardRegistration(set = "TMT", collectorNumber = "233")
public class SplintersTechnique extends Card {

    public SplintersTechnique() {
        addSneak("{1}{B}");
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect());
    }
}
