package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "332")
@CardRegistration(set = "4ED", collectorNumber = "276")
public class TitaniasSong extends Card {

    public TitaniasSong() {
        // Each noncreature artifact loses all abilities and becomes an artifact creature
        // with power and toughness each equal to its mana value.
        addEffect(EffectSlot.STATIC, new AnimateNoncreatureArtifactsEffect(true));
    }
}
