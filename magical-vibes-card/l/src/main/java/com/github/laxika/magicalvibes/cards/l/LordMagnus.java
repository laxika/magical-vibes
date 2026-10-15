package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.LandwalkIgnoredForBlockingEffect;

@CardRegistration(set = "LEG", collectorNumber = "243")
public class LordMagnus extends Card {

    public LordMagnus() {
        // Creatures with plainswalk or forestwalk can be blocked as though they didn't have those abilities.
        addEffect(EffectSlot.STATIC, new LandwalkIgnoredForBlockingEffect(Keyword.PLAINSWALK));
        addEffect(EffectSlot.STATIC, new LandwalkIgnoredForBlockingEffect(Keyword.FORESTWALK));
    }
}
