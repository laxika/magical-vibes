package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AirbendTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLA", collectorNumber = "8")
public class AirbendingLesson extends Card {

    public AirbendingLesson() {
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL, new AirbendTargetPermanentEffect())
                .addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
