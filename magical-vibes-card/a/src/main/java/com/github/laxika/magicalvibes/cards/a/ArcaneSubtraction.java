package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.LearnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "STX", collectorNumber = "36")
public class ArcaneSubtraction extends Card {

    public ArcaneSubtraction() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(-4, 0));

        addSpellEffect(new LearnEffect());
    }
}
