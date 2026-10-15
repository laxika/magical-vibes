package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LGN", collectorNumber = "127")
@CardRegistration(set = "DD1", collectorNumber = "6")
@CardRegistration(set = "EVG", collectorNumber = "6")
public class GempalmStrider extends Card {

    public GempalmStrider() {
        addEffect(EffectSlot.ON_SELF_CYCLED,
                new BoostAllCreaturesEffect(2, 2,
                        new PermanentHasSubtypePredicate(CardSubtype.ELF)));
        addCycling("{2}{G}{G}");
    }
}
