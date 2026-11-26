package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ONE", collectorNumber = "117")
public class WhisperOfTheDross extends Card {

    public WhisperOfTheDross() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(-1, -1))
                .addSpellEffect(new ProliferateEffect());
    }
}
