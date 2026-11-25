package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;

@CardRegistration(set = "AER", collectorNumber = "39")
@CardRegistration(set = "2XM", collectorNumber = "59")
@CardRegistration(set = "KLR", collectorNumber = "56")
public class MetallicRebuke extends Card {

    public MetallicRebuke() {
        addSpellEffect(UnlessPaysEffect.counterUnlessPays(3));
    }
}
