package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllLandsAreCreaturesEffect;

@CardRegistration(set = "4ED", collectorNumber = "332")
public class KormusBell extends Card {

    public KormusBell() {
        // All Swamps are 1/1 black creatures that are still lands. A Swamp has no mana cost and is
        // therefore colourless on its own (CR 202.2), so the black has to come from this effect.
        addEffect(EffectSlot.STATIC,
                new AllLandsAreCreaturesEffect(1, 1, CardSubtype.SWAMP, CardColor.BLACK));
    }
}
