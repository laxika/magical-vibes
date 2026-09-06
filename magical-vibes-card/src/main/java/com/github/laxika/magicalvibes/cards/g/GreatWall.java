package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.LandwalkIgnoredForBlockingEffect;

@CardRegistration(set = "LEG", collectorNumber = "17")
public class GreatWall extends Card {

    public GreatWall() {
        addEffect(EffectSlot.STATIC, new LandwalkIgnoredForBlockingEffect(Keyword.PLAINSWALK));
    }
}
